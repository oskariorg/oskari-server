package org.oskari.cluster;

import fi.nls.oskari.cache.JedisManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClusterClientTest {

    @Test
    public void testPubSub() throws Exception {
        Assumptions.assumeTrue(redisAvailable());

        final List<String> firstSubMessages = new ArrayList<>();
        final List<String> secondSubMessages = new ArrayList<>();
        final ClusterClient sub1 = new ClusterClient("test");
        sub1.addListener("testChannel", (msg) -> {
            firstSubMessages.add(msg);
            //System.out.println("in listener: " + firstSubMessages.size());
        });
        sub1.addListener("testChannel2", (msg) -> firstSubMessages.add(msg));

        final ClusterClient sub2 = new ClusterClient("test");
        sub2.addListener("testChannel", (msg) -> {
            secondSubMessages.add(msg);
            //System.out.println(secondSubMessages.size());
        });
        sub2.addListener("testChannel2", (msg) -> secondSubMessages.add(msg));

        // give time for subscriptions to complete
        Thread.sleep(500);

        String sentMessage = "test message";
        long res = JedisManager.publish(
                ClusterClient.getChannel("test", "testChannel"),
                createClusterMsgOther("test message"));
        Assertions.assertEquals(2, res, "Client count should be 2");

        // messages require some time to go through
        Thread.sleep(500);

        //System.out.println("before assert: " + firstSubMessages.size());
        Assertions.assertEquals(1, firstSubMessages.size(), "First sub should have 1 msg");
        Assertions.assertNotNull(firstSubMessages.get(0));
        Assertions.assertEquals(sentMessage, firstSubMessages.get(0), "First sub should have the msg we sent");
        Assertions.assertEquals(1, secondSubMessages.size(), "Second sub should have 1 msg");
        Assertions.assertNotNull(secondSubMessages.get(0));
        Assertions.assertEquals(sentMessage, secondSubMessages.get(0), "Second sub should have the msg we sent");

        res = JedisManager.publish(
                ClusterClient.getChannel("test", "testChannel2"),
                createClusterMsgOther("test message"));
        Assertions.assertEquals(2, res, "Client count should be 2");

        // messages require some time to go through
        Thread.sleep(500);

        Assertions.assertEquals(2, firstSubMessages.size(), "First sub should have 2 msgs");
        Assertions.assertNotNull(firstSubMessages.get(1));
        Assertions.assertEquals(sentMessage, firstSubMessages.get(1), "First sub second msg should be the one we sent");
        Assertions.assertEquals(2, secondSubMessages.size(), "Second sub should have 2 msgs");
        Assertions.assertNotNull(secondSubMessages.get(1));
        Assertions.assertEquals(sentMessage, secondSubMessages.get(1), "Second sub second msg should be the one we sent");

        res = JedisManager.publish(
                ClusterClient.getChannel("test", "testChannel2"),
                createClusterMsgSelf("test message"));
        Assertions.assertEquals(2, res, "Client count should be 2");
        Assertions.assertEquals(2, firstSubMessages.size(), "First sub should still have 2 msgs as msg was from same node");
        Assertions.assertEquals(2, secondSubMessages.size(), "Second sub should still have 2 msgs as msg was from same node");

        sub1.stopListening();
        sub2.stopListening();
    }

    /**
     * Checks if we are able to connect to redis with a simple scenario
     * No point in running Redis tests if it is not available on the env we are running the test.
     * @return
     */
    private static boolean redisAvailable() {
        // once we call connect here we don't need to connect on the actual test
        JedisManager.connect();
        final String testKey = "testing";
        final String testValue = "availability";
        final String msg = JedisManager.setex(testKey, 10, testValue);
        if (msg == null) {
            return false;
        }
        return testValue.equals(JedisManager.get(testKey));
    }

    private String createClusterMsgSelf(String msg) {
        return ClusterManager.getInstance().getId() + "_" + msg;
    }
    private String createClusterMsgOther(String msg) {
        return UUID.randomUUID().toString() + "_" + msg;
    }
}
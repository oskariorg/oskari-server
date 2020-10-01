package org.oskari.cache;

import fi.nls.oskari.cache.JedisManager;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class JedisSubscriberClientTest {

    @Test
    public void testPubSub() throws Exception {
        org.junit.Assume.assumeTrue(redisAvailable());

        final List<String> sub1channel1 = new ArrayList<>();
        final List<String> sub2channel1 = new ArrayList<>();
        final List<String> sub1channel2 = new ArrayList<>();
        final List<String> sub2channel2 = new ArrayList<>();
        final JedisSubscriberClient sub1 = new JedisSubscriberClient("test");
        sub1.addListener("testChannel", (msg) -> {
            sub1channel1.add(msg);
            //System.out.println("in listener: " + sub1channel1.size());
        });
        sub1.addListener("testChannel2", (msg) -> sub1channel2.add(msg));

        final JedisSubscriberClient sub2 = new JedisSubscriberClient("test");
        sub2.addListener("testChannel", (msg) -> {
            sub2channel1.add(msg);
            //System.out.println(sub2channel1.size());
        });
        sub2.addListener("testChannel2", (msg) -> sub2channel2.add(msg));

        // give time for subscriptions to complete
        Thread.sleep(500);

        String sentMessage = "test message";
        long res = JedisManager.publish(
                JedisSubscriberClient.getChannel("test", "testChannel"),
                "test message");
        assertEquals("Client count should be 2", 2, res);

        Thread.sleep(500);

        //System.out.println("before assert: " + sub1channel1.size());
        assertEquals(1, sub1channel1.size());
        assertNotNull(sub1channel1.get(0));
        assertEquals(sentMessage, sub1channel1.get(0));
        assertEquals(1, sub2channel1.size());
        assertNotNull(sub2channel1.get(0));
        assertEquals(sentMessage, sub2channel1.get(0));

        res = JedisManager.publish(
                JedisSubscriberClient.getChannel("test", "testChannel2"),
                "test message");
        assertEquals("Client count should be 2", 2, res);

        Thread.sleep(500);
        assertEquals(1, sub1channel2.size());
        assertNotNull(sub1channel2.get(0));
        assertEquals(sentMessage, sub1channel2.get(0));
        assertEquals(1, sub2channel2.size());
        assertNotNull(sub2channel2.get(0));
        assertEquals(sentMessage, sub2channel2.get(0));

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
}
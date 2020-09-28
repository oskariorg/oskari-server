package org.oskari.cache;

import fi.nls.oskari.cache.JedisManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class JedisSubscriberClientTest {

    @Test
    public void testPubSub() throws Exception {
        org.junit.Assume.assumeTrue(redisAvailable());

        final String[] receivedMessage = new String[1];
        final String[] receivedMessage2 = new String[1];
        final JedisSubscriberClient sub1 = new JedisSubscriberClient("test", (msg) -> receivedMessage[0] = msg);
        final JedisSubscriberClient sub2 = new JedisSubscriberClient("test", (msg) -> receivedMessage2[0] = msg);

        // give time for subscriptions to complete
        Thread.sleep(500);

        String sentMessage = "test message";
        Long res = JedisManager.publish("test", "test message");
        assertTrue(res == 2);

        assertNotNull(receivedMessage[0]);
        assertEquals(receivedMessage[0], sentMessage);
        assertNotNull(receivedMessage2[0]);
        assertEquals(receivedMessage2[0], sentMessage);

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
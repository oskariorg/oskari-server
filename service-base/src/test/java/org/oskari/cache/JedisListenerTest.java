package org.oskari.cache;

import fi.nls.oskari.cache.JedisManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class JedisListenerTest {

    @Test
    public void testPubSub() throws Exception {
        final String[] receivedMessage = {null};
        final String[] receivedMessage2 = {null};
        final JedisListener sub1 = new JedisListener("test") {
            @Override
            public void onMessage(String channel, String message) {
                receivedMessage[0] = message;
            }
        };

        final JedisListener sub2 = new JedisListener("test") {
            @Override
            public void onMessage(String channel, String message) {
                receivedMessage2[0] = message;
            }
        };

        Thread.sleep(500); // give time for subscriptions to complete
        String sentMessage = "test message";
        JedisManager.connect();
        Long res = JedisManager.publish("test", "test message");
        assertTrue(res == 2);
        assertNotNull(receivedMessage[0]);
        assertEquals(receivedMessage[0], sentMessage);
        assertNotNull(receivedMessage2[0]);
        assertEquals(receivedMessage2[0], sentMessage);
        sub1.stopListening();
        sub2.stopListening();
    }
}
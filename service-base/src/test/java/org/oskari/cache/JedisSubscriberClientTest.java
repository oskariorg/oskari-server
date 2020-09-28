package org.oskari.cache;

import fi.nls.oskari.cache.JedisManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class JedisSubscriberClientTest {

    @Test
    public void testPubSub() throws Exception {
        final String[] receivedMessage = new String[1];
        final String[] receivedMessage2 = new String[1];
        final JedisSubscriberClient sub1 = new JedisSubscriberClient("test", (msg) -> receivedMessage[0] = msg);
        final JedisSubscriberClient sub2 = new JedisSubscriberClient("test", (msg) -> receivedMessage2[0] = msg);

        Thread.sleep(500); // give time for subscriptions to complete
        String sentMessage = "test message";
        JedisManager.connect();
        Long res = JedisManager.publish("test", "test message");
        assertTrue(res == 2);
        System.out.println(receivedMessage[0]);
        assertNotNull(receivedMessage[0]);
        assertEquals(receivedMessage[0], sentMessage);
        assertNotNull(receivedMessage2[0]);
        assertEquals(receivedMessage2[0], sentMessage);
        sub1.stopListening();
        sub2.stopListening();
    }
}
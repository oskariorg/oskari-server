package fi.nls.oskari.cache;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import redis.clients.jedis.JedisPubSub;

public class JedisSubscriber extends JedisPubSub {

    private final static Logger log = LogFactory.getLogger(JedisSubscriber.class);

    /**
     * Triggered when message is received
     *
     * @param channel
     * @param message
     */
    @Override
    public void onMessage(String channel, String message) {
        log.warn("Message received. Channel: " + channel + " Message: " + message);

    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {

    }

    /**
     * Triggered when subscribed
     *
     * @param channel
     * @param subscribedChannels
     */
    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        log.debug("Subscribed. Channel: " + channel + " Channel count: " + subscribedChannels);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {

    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {

    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {

    }
}

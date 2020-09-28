package org.oskari.cache;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.OskariRuntimeException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JedisSubscriberClient extends JedisPubSub {

    private final static Logger LOG = LogFactory.getLogger(JedisSubscriberClient.class);
    private ExecutorService service = Executors.newFixedThreadPool(1);
    private String subscribedChannel;
    private MessageListener listener;

    public JedisSubscriberClient(String channel, MessageListener listener) {
        if (channel == null || listener == null) {
            throw new OskariRuntimeException("Requires channel and listener");
        }
        subscribedChannel = JedisManager.PUBSUB_CHANNEL_PREFIX + channel;
        this.listener = listener;
        startListening(subscribedChannel);
    }

    private void startListening(String channel) {
        // if subscribe raises en exception the Thread will end
        // and the executor will execute the next task -> reconnecting the client
        service.execute(() -> {
            try (Jedis jedis = createClient())  {
                LOG.info("Subscribing on", channel);
                // Subscribe is a blocking action hence the thread
                // Also we don't care about pooling here since
                // the client remains blocked for subscription
                jedis.subscribe(this, channel);
            } catch (Exception e) {
                LOG.error(e,"Problem listening to channel:", channel);
            }
        });
    }

    // https://redis.io/topics/pubsub
    // "A client subscribed to one or more channels should not issue commands,
    // although it can subscribe and unsubscribe to and from other channels."
    // "Make sure the subscriber and publisher threads do not share the same Jedis connection."
    // NOTE!! create a new client for subscriptions instead of using pool to make sure clients don't conflict
    private Jedis createClient() {
        return new Jedis(JedisManager.getHost(), JedisManager.getPort());
    }

    @Override
    public void onMessage(String channel, String message) {
        if (subscribedChannel.equals(channel)) {
            listener.onMessage(message);
        }
    }

    public void stopListening() {
        try {
            // shutdown thread so it's not reconnecting
            service.shutdown();
        } catch (Exception ignored) {
            LOG.ignore("Error shutting down listener thread", ignored);
        }
        try {
            // unsubscribe from Redis
            // closes the client it was passed as well
            this.unsubscribe();
        } catch (Exception ignored) {
            LOG.ignore("Error unsubscribing while shutting down", ignored);
        }
    }
}

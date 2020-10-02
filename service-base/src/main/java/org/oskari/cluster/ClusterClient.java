package org.oskari.cluster;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.OskariRuntimeException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClusterClient extends JedisPubSub {

    private final static Logger LOG = LogFactory.getLogger(ClusterClient.class);
    private ExecutorService service = Executors.newFixedThreadPool(1);
    private String functionalityId;
    private Jedis client;
    private Map<String, List<MessageListener>> listeners = new HashMap<>();

    /**
     * Same as JedisManager.publish() but this uses the same functionality id <> channel separation as when
     * listening to the messages.
     *
     * Mostly for documentary purposes on how you SHOULD send a message so the subscriber with the same
     * functionality id will catch it.
     *
     * @param functionalityId
     * @param channel
     * @param message
     * @return
     */
    public static long sendMessage(String functionalityId, String channel, String message) {
        // we can use the same client to send AND subscribe so using JdeisManagers pooled connections for sending
        return JedisManager.publish(ClusterClient.getChannel(functionalityId, channel), message);
    }

    /**
     * For sending messages with JedisManager you can use this method to construct the channel for the functionality id/channel combo.
     * @param functionalityId
     * @param channel
     * @return
     */
    public static String getChannel(String functionalityId, String channel) {
        return functionalityId + "_" + channel;
    }

    public ClusterClient(String functionalityId) {
        if (functionalityId == null) {
            throw new OskariRuntimeException("Requires functionalityId");
        }
        this.functionalityId = functionalityId;
        startListening(getFullChannelPrefix());
    }

    /**
     * The main method for listening to messages for the functionality
     * @param channel
     * @param listener
     */
    public void addListener(String channel, MessageListener listener) {
        List<MessageListener> existingListeners = listeners.computeIfAbsent(channel, key -> new ArrayList<>());
        existingListeners.add(listener);
    }

    /**
     * Same as the other sendMessage but since it's called through the subscriber instance we already know the functionality id.
     * @param channel
     * @param message
     * @return
     */
    public long sendMessage(String channel, String message) {
        return ClusterClient.sendMessage(functionalityId, channel, message);
    }

    /**
     * Removes listeners and closes connection to Redis. A "destroy"/cleanup method and you can't use the subscriber
     * after calling this.
     */
    public void stopListening() {
        listeners.clear();
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

    /**
     * Not meant to be overridden. It's just a method we are overriding from JedisPubSub.
     * @param pattern
     * @param channel
     * @param message
     */
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        getListeners(channel).stream().forEach(l -> l.onMessage(message));
    }

    private String getFullChannelPrefix() {
        return JedisManager.PUBSUB_CHANNEL_PREFIX + functionalityId + "_";
    }

    private List<MessageListener> getListeners(String channel) {
        String prefix = getFullChannelPrefix();
        if (channel == null || !channel.startsWith(prefix)) {
            return Collections.emptyList();
        }
        String key = channel.substring(prefix.length());
        return listeners.getOrDefault(key, Collections.emptyList());
    }

    private void startListening(String prefix) {
        // if subscribe raises en exception the Thread will end
        // and the executor will execute the next task -> reconnecting the client
        service.execute(() -> {
            try (Jedis jedis = createClient())  {
                LOG.info("Subscribing to all channels starting with", prefix);
                // Subscribe is a blocking action hence the thread
                // Also we don't care about pooling here since
                // the client remains blocked for subscription
                jedis.psubscribe(this, prefix + "*");
            } catch (Exception e) {
                LOG.error(e,"Problem listening to channel:", prefix);
            } finally {
                client = null;
            }
        });
    }

    // https://redis.io/topics/pubsub
    // "A client subscribed to one or more channels should not issue commands,
    // although it can subscribe and unsubscribe to and from other channels."
    // "Make sure the subscriber and publisher threads do not share the same Jedis connection."
    // NOTE!! create a new client for subscriptions instead of using pool to make sure clients don't conflict
    private Jedis createClient() {
        if (client == null) {
            client = new Jedis(JedisManager.getHost(), JedisManager.getPort());
        }
        return client;
    }

}

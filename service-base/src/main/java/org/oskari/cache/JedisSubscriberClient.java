package org.oskari.cache;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.OskariRuntimeException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JedisSubscriberClient extends JedisPubSub {

    private final static Logger LOG = LogFactory.getLogger(JedisSubscriberClient.class);
    private ExecutorService service = Executors.newFixedThreadPool(1);
    private String functionalityId;
    private Jedis client;
    private Map<String, List<MessageListener>> listeners = new HashMap<>();

    public JedisSubscriberClient(String functionalityId) {
        if (functionalityId == null) {
            throw new OskariRuntimeException("Requires functionalityId");
        }
        this.functionalityId = functionalityId;
        startListening(getFullChannelPrefix());
    }

    public static String getChannel(String functionalityId, String channel) {
        return functionalityId + "_" + channel;
    }

    public static long sendMessage(String functionalityId, String channel, String message) {
        return JedisManager.publish(JedisSubscriberClient.getChannel(functionalityId, channel), message);
    }

    private String getFullChannelPrefix() {
        return JedisManager.PUBSUB_CHANNEL_PREFIX + functionalityId + "_";
    }

    public String getChannel(String channel) {
        return functionalityId + "_" + channel;
    }

    public long sendMessage(String channel, String message) {
        return JedisManager.publish(JedisSubscriberClient.getChannel(functionalityId, channel), message);
    }


    public void addListener(String channel, MessageListener listener) {
        List<MessageListener> existingListeners = listeners.computeIfAbsent(channel, key -> new ArrayList<>());
        existingListeners.add(listener);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        getListeners(channel).stream().forEach(l -> l.onMessage(message));
    }

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

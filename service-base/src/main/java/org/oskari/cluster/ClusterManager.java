package org.oskari.cluster;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClusterManager {

    private final static Logger LOG = LogFactory.getLogger(ClusterManager.class);
    private static ClusterManager instance;

    private final ConcurrentMap<String, ClusterClient> clients = new ConcurrentHashMap<>();
    // so we can identify our own messages from other nodes messages
    private final String clusterNodeId;

    private ClusterManager() {
        clusterNodeId = UUID.randomUUID().toString();
    }

    /**
     * Singleton method. Mostly for enabling easier JUnit testing.
     * @return
     */
    public synchronized static ClusterManager getInstance() {
        if (instance == null) {
            instance = new ClusterManager();
        }
        return instance;
    }

    /**
     * Returns a shared ClusterClient for given functionality (like "cache") that can be used to send/listen to messages
     * between cluster nodes.
     * @param functionalityId identifier so different functionalities can have separate subscriptions for simpler protocol
     * @return
     */
    public static ClusterClient getClientFor(String functionalityId) {
        return getInstance().clients.computeIfAbsent(functionalityId, key -> new ClusterClient(functionalityId));
    }

    /**
     * Creates a message that is prefixed with the cluster nodes instance id that is used to detect messages from self
     * @param msg
     * @return
     */
    public static String createClusterMsg(String msg) {
        if (!isClustered()) {
            return msg;
        }
        return getId() + "_" + msg;
    }

    /**
     * Interprets the message to determine if it was from the same node or another one and removes the cluster protocol
     * parts to make it easier for clients to have their own protocol without knowledge of the cluster messaging details
     *
     * @param message the raw data that came from listening to redis pubsub
     * @return the message without cluster protocol parts or null if the message was from the same node/couldn't be interpreted
     */
    public static String readClusterMsg(String message) {
        if (message == null) {
            return null;
        }
        String instanceId = getId();
        String myPrefix = instanceId + "_";
        if (message.startsWith(myPrefix)) {
            // this is my own message -> ignore it
            LOG.debug("Got my own message:", instanceId);
            return null;
        }
        if (message.length() < myPrefix.length()) {
            LOG.warn("Cluster protocol error. Message not prefixed by instanceId:", message);
            return null;
        }
        String msg = message.substring(myPrefix.length());
        if (msg.trim().isEmpty()) {
            return null;
        }
        return msg;
    }

    /**
     * Returns the id for this cluster node. Used to detect if messages come from self or another node.
     * @return
     */
    public static String getId() {
        return getInstance().clusterNodeId;
    }

    /**
     * Same as JedisManager.isClusterEnv() but for convenience so we don't have to check there and also use ClusterManager.
     * @return
     */
    public static boolean isClustered() {
        return JedisManager.isClusterEnv();
    }
}

package org.oskari.cluster;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.Optional;
import java.util.OptionalInt;
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

    public synchronized static ClusterManager getInstance() {
        if (instance == null) {
            instance = new ClusterManager();
        }
        return instance;
    }

    public static ClusterClient getClientFor(String functionalityId) {
        return getInstance().clients.computeIfAbsent(functionalityId, key -> new ClusterClient(functionalityId));
    }

    public static String createClusterMsg(String msg) {
        if (!isClustered()) {
            return msg;
        }
        return getId() + "_" + msg;
    }

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

    public static String getId() {
        return getInstance().clusterNodeId;
    }

    public static boolean isClustered() {
        return JedisManager.isClusterEnv();
    }
}

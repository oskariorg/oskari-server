package org.oskari.cluster;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClusterManager {

    private static ClusterManager instance;

    private final ConcurrentMap<String, ClusterClient> clients = new ConcurrentHashMap<>();
    private final String cacheInstanceId;

    private ClusterManager() {
        cacheInstanceId = UUID.randomUUID().toString();
    }
    public static ClusterManager getInstance() {
        if (instance == null) {
            instance = new ClusterManager();
        }
        return instance;
    }

    public ClusterClient getClientFor(String functionalityId) {
        return clients.computeIfAbsent(functionalityId, key -> new ClusterClient(functionalityId));
    }
}

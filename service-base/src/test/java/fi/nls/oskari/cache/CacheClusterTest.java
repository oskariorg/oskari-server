package fi.nls.oskari.cache;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.oskari.cluster.ClusterManager;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

/**
 * Simple tests for cache.
 */
public class CacheClusterTest {

    @After
    public void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testClusterMsgFromOthersFlush() {
        Cache<String> cache = spy(Cache.class);
        cache.setName("Clustered");
        // another cache instance
        cache.handleClusterMsg(Cache.CLUSTER_CMD_FLUSH);
        // message from OTHER should trigger call
        Mockito.verify(cache).flushSilent(true);
        // but not trigger another notify for cluster
        Mockito.verify(cache, never()).flush(true);
    }

    @Test
    public void testClusterMsgDelete() {
        String cacheKey = "testing";
        Cache<String> cache = spy(Cache.class);
        cache.setName("Clustered");
        // another cache instance
        cache.handleClusterMsg(Cache.CLUSTER_CMD_REMOVE_PREFIX + cacheKey);
        // message from OTHER should trigger call
        Mockito.verify(cache).removeSilent(cacheKey);
        // but not trigger another notify for cluster
        Mockito.verify(cache, never()).remove(cacheKey);
    }
}

package fi.nls.oskari.cache;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

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
    public void testClusterMsgFromSelfFlush() {
        Cache<String> cache = spy(Cache.class);
        cache.setName("Clustered");
        cache.handleClusterMsg(cache.getCacheInstanceId() + "_" + Cache.CLUSTER_CMD_FLUSH);
        // message from self should NOT trigger call
        Mockito.verify(cache, never()).flush(true);
    }

    @Test
    public void testClusterMsgFromOthersFlush() {
        Cache<String> cache = spy(Cache.class);
        cache.setName("Clustered");
        // another cache instance
        cache.handleClusterMsg(UUID.randomUUID().toString() + "_" + Cache.CLUSTER_CMD_FLUSH);
        // message from OTHER should trigger call
        Mockito.verify(cache).flush(true);
    }

    @Test
    public void testClusterMsgFromSelfDelete() {
        String cacheKey = "testing";
        Cache<String> cache = spy(Cache.class);
        cache.setName("Clustered");
        cache.handleClusterMsg(cache.getCacheInstanceId() + "_" + Cache.CLUSTER_CMD_REMOVE_PREFIX + cacheKey);
        // message from self should NOT trigger call
        Mockito.verify(cache, never()).remove(cacheKey);
        Mockito.verify(cache, never()).removeSilent(cacheKey);
    }

    @Test
    public void testClusterMsgFromOthersDelete() {
        String cacheKey = "testing";
        Cache<String> cache = spy(Cache.class);
        cache.setName("Clustered");
        // another cache instance
        cache.handleClusterMsg(UUID.randomUUID().toString() + "_" + Cache.CLUSTER_CMD_REMOVE_PREFIX + cacheKey);
        // message from OTHER should trigger call
        Mockito.verify(cache).removeSilent(cacheKey);
        // but not trigger another notify for cluster
        Mockito.verify(cache, never()).remove(cacheKey);
    }
}

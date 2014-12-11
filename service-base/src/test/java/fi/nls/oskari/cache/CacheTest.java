package fi.nls.oskari.cache;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Simple tests for cache.
 */
public class CacheTest {

    @After
    public void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testLimitNoProperty() {
        Cache<String> cache = CacheManager.getCache("LimitNoProperty");
        assertEquals("Cache should be empty", 0, cache.getSize());
        final int limit = 10;
        cache.setLimit(limit);
        assertEquals("Cache limit should be " + limit, limit, cache.getLimit());
        for(int i = 0; i < limit + 5; i++) {
            boolean overflow = cache.put("test" + i, "testing");
            if(i < limit) {
                assertFalse("Not overflowing", overflow);
            }
            else {
                assertTrue("Overflowing", overflow);
            }
        }
        assertEquals("Cache size should be " + limit, limit, cache.getSize());
    }

    @Test
    public void testLimitWithProperty() throws Exception {
        final String cacheName = "LimitWithProperty";
        final int limit = 5;
        PropertyUtil.addProperty(Cache.PROPERTY_LIMIT_PREFIX + cacheName, "" + limit);

        Cache<String> cache = CacheManager.getCache(cacheName);
        assertEquals("Cache should be empty", 0, cache.getSize());
        assertEquals("Cache limit should be " + limit, limit, cache.getLimit());

        cache.setLimit(limit + 5);
        assertEquals("Cache limit prefers property config " + limit, limit, cache.getLimit());

        for(int i = 0; i < limit + 5; i++) {
            boolean overflow = cache.put("test" + i, "testing");
            if(i < limit) {
                assertFalse("Not overflowing", overflow);
            }
            else {
                assertTrue("Overflowing", overflow);
            }
        }
        assertEquals("Cache size should be " + limit, limit, cache.getSize());
    }

    @Test
    public void testExpiration() {
        final String cacheName = "Expiration";
        final Cache<String> cache = CacheManager.getCache(cacheName);
        final long last = cache.getLastFlush();
        assertFalse("Newly created cache should not be ready to flush", cache.isTimeToFlush(last));
        final long expiration = cache.getExpiration();
        assertTrue("Cache lastFlush + expiration + 10 should be cleared for flush", cache.isTimeToFlush(last + expiration + 10));
    }

}

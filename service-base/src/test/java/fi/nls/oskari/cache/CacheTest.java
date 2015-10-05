package fi.nls.oskari.cache;

import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

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
            boolean overflow = cache.put("test" + i, "testing" + i);
            if(i < limit) {
                assertFalse("Not overflowing", overflow);
            }
            else {
                assertTrue("Overflowing", overflow);
            }
        }
        // Checking that the oldest elements have been removed and the remaining are test5-test14
        for(String key : cache.getKeys()) {
            final int num = ConversionHelper.getInt(key.substring(4), -1);
            String value = cache.get(key);
            final int num2 = ConversionHelper.getInt(value.substring(7), -1);
            assertTrue("Keynumbers should be over 4/" + num + "/" + key, num > 4);
            assertTrue("Values should be over 4/" + num2 + "/" + value, num2 > 4);
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

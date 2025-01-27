package fi.nls.oskari.cache;

import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Simple tests for cache.
 */
public class CacheTest {

    @AfterEach
    public void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testLimitNoProperty() {
        Cache<String> cache = CacheManager.getCache("LimitNoProperty");
        Assertions.assertEquals(0, cache.getSize(), "Cache should be empty");
        final int limit = 10;
        cache.setLimit(limit);
        Assertions.assertEquals(limit, cache.getLimit(), "Cache limit should be " + limit);
        for(int i = 0; i < limit + 5; i++) {
            boolean overflow = cache.put("test" + i, "testing" + i);
            if(i < limit) {
                Assertions.assertFalse(overflow, "Not overflowing");
            }
            else {
                Assertions.assertTrue(overflow, "Overflowing");
            }
        }
        // Checking that the oldest elements have been removed and the remaining are test5-test14
        for(String key : cache.getKeys()) {
            final int num = ConversionHelper.getInt(key.substring(4), -1);
            String value = cache.get(key);
            final int num2 = ConversionHelper.getInt(value.substring(7), -1);
            Assertions.assertTrue(num > 4, "Keynumbers should be over 4/" + num + "/" + key);
            Assertions.assertTrue(num2 > 4, "Values should be over 4/" + num2 + "/" + value);
        }
        Assertions.assertEquals(limit, cache.getSize(), "Cache size should be " + limit);
    }

    @Test
    public void testLimitWithProperty() throws Exception {
        final String cacheName = "LimitWithProperty";
        final int limit = 5;
        PropertyUtil.addProperty(Cache.PROPERTY_LIMIT_PREFIX + cacheName, "" + limit);

        Cache<String> cache = CacheManager.getCache(cacheName);
        Assertions.assertEquals(0, cache.getSize(), "Cache should be empty");
        Assertions.assertEquals(limit, cache.getLimit(), "Cache limit should be " + limit);

        cache.setLimit(limit + 5);
        Assertions.assertEquals(limit, cache.getLimit(), "Cache limit prefers property config " + limit);

        for(int i = 0; i < limit + 5; i++) {
            boolean overflow = cache.put("test" + i, "testing");
            if(i < limit) {
                Assertions.assertFalse(overflow, "Not overflowing");
            }
            else {
                Assertions.assertTrue(overflow, "Overflowing");
            }
        }
        Assertions.assertEquals(limit, cache.getSize(), "Cache size should be " + limit);
    }

    @Test
    public void testExpiration() {
        final String cacheName = "Expiration";
        final Cache<String> cache = CacheManager.getCache(cacheName);
        final long last = cache.getLastFlush();
        Assertions.assertFalse(cache.isTimeToFlush(last), "Newly created cache should not be ready to flush");
        final long expiration = cache.getExpiration();
        Assertions.assertTrue(cache.isTimeToFlush(last + expiration + 10), "Cache lastFlush + expiration + 10 should be cleared for flush");
    }

}

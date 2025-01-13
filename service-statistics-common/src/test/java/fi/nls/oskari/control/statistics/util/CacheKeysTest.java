package fi.nls.oskari.control.statistics.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class CacheKeysTest {

    @Test
    public void buildCacheKey() {
        assertEquals("oskari:stats:2", CacheKeys.buildCacheKey(2));
        assertEquals("oskari:stats:2:list", CacheKeys.buildCacheKey(2, "list"));
        assertEquals("oskari:stats:7:indicator:5", CacheKeys.buildCacheKey(7, "indicator", 5));
    }
}
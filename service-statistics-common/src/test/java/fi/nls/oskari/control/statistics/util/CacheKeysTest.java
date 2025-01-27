package fi.nls.oskari.control.statistics.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CacheKeysTest {

    @Test
    public void buildCacheKey() {
        Assertions.assertEquals("oskari:stats:2", CacheKeys.buildCacheKey(2));
        Assertions.assertEquals("oskari:stats:2:list", CacheKeys.buildCacheKey(2, "list"));
        Assertions.assertEquals("oskari:stats:7:indicator:5", CacheKeys.buildCacheKey(7, "indicator", 5));
    }
}
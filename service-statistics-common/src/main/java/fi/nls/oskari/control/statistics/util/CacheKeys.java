package fi.nls.oskari.control.statistics.util;

import java.util.Arrays;

public class CacheKeys {
    public static final String CACHE_PREFIX = "oskari:stats:";
    public static final char CACHE_KEY_SEPARATOR = ':';

    public static String buildCacheKey(long datasourceId, Object... identifiers) {
        StringBuilder cacheKey = new StringBuilder(CacheKeys.CACHE_PREFIX);
        cacheKey.append(datasourceId);
        Arrays.stream(identifiers).forEach(item -> {
            cacheKey.append(CACHE_KEY_SEPARATOR);
            cacheKey.append(item);
        });
        return cacheKey.toString();
    }
}

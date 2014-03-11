package fi.nls.oskari.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 11.3.2014
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
public class CacheManager {

    private static Map<String,Cache> cacheStore = new HashMap<String, Cache>();

    public static <T> Cache<T> getCache(final String name) {
        final Cache existing = cacheStore.get(name);
        if(existing != null) {
            return existing;
        }

        // create a new one
        final Cache<T> cache = new Cache<T>();
        cache.setName(name);
        cacheStore.put(name, cache);
        return cache;
    }

    /**
     * Adds cache manually to manager.
     * @param name
     * @param cache
     * @return true if successful, false if params missing or a cache already exists with same name
     */
    public static boolean addCache(final String name, final Cache cache) {
        if(name == null || cache == null || cacheStore.containsKey(name)) {
            return false;
        }
        cache.setName(name);
        cacheStore.put(name, cache);
        return true;
    }

}

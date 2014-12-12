package fi.nls.oskari.cache;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Generic cache factory for Oskari.
 * TODO: make Cache implementation parametrizable via PropertyUtil. Same as logger and UserService.
 * Usage:
 * <pre>
 * {@code
 *  private Cache<String> cache = CacheManager.getCache("MyCacheName");
 *  final String response = getUrl("http://www.paikkatietoikkuna.fi");
 *
 *  public String getUrl(final String url) {
 *      String response = cache.get(propertyName);
 *      if(response != null) {
 *          return response;
 *      }
 *      response = IOHelper.getURL(url);
 *      cache.put(url, response);
 *      return response;
 *  }
 * }
 * </pre>
 */
public class CacheManager {

    private static ConcurrentMap<String, Cache> CACHE_STORE = new ConcurrentHashMap<String, Cache>();

    /**
     * Returns a cache matching name or creates one if it doesn't exist.
     *
     * @param name name of the cache
     * @param <T>  type mapping for cache
     * @return
     */
    public static <T> Cache<T> getCache(final String name) {
        final Cache existing = CACHE_STORE.get(name);
        if (existing != null) {
            return existing;
        }

        // create a new one
        final Cache<T> cache = new Cache<T>();
        cache.setName(name);
        CACHE_STORE.put(name, cache);
        return cache;
    }

    /**
     * Returns names of registered caches
     */
    public static Set<String> getCacheNames() {
        return CACHE_STORE.keySet();
    }

    /**
     * Registers cache manually to manager.
     * @return true if successful, false if params missing or a cache already exists with same name
     */
    public static boolean addCache(final String name, final Cache cache) {
        if (null == name || null == cache || CACHE_STORE.containsKey(name)) {
            return false;
        }
        cache.setName(name);
        CACHE_STORE.put(name, cache);
        return true;
    }

}

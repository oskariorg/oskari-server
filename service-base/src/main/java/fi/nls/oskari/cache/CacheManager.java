package fi.nls.oskari.cache;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

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

    private static final ConcurrentMap<String, Cache> CACHE_STORE = new ConcurrentHashMap<>();

    /**
     * Returns a cache matching name or creates one if it doesn't exist.
     *
     * @param name name of the cache
     * @param <T>  type mapping for cache
     * @return
     */
    public static <T> Cache<T> getCache(final String name) {
        return getCache(name, () -> new Cache<>());
    }

    /**
     * Returns a cache matching name or creates one if it doesn't exist
     * by calling the provided supplier function.
     *
     * @param name name of the cache
     * @param supplier function which creates a new cache if one doesn't exists for the key
     * @param <T>  type mapping for cache
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T1 extends Cache<T2>, T2> T1 getCache(final String name, final Supplier<T1> supplier) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(supplier);
        return (T1) CACHE_STORE.computeIfAbsent(name, __ -> {
            Cache<T2> cache = supplier.get();
            cache.setName(name);
            return cache;
        });
    }

    /**
     * Returns names of registered caches
     */
    public static Set<String> getCacheNames() {
        return CACHE_STORE.keySet();
    }

    /**
     * @deprecated to be removed, use {@link #getCache(String)}
     *
     * Registers cache manually to manager.
     * @return true if successful, false if params missing or a cache already exists with same name
     */
    public static <T> boolean addCache(final String name, final Cache<T> cache) {
        if (null == name || null == cache || CACHE_STORE.containsKey(name)) {
            return false;
        }
        cache.setName(name);
        CACHE_STORE.put(name, cache);
        return true;
    }

}

package fi.nls.oskari.cache;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Simple generic in memory cache
 */
public class Cache<T> {

    private static final Logger log = LogFactory.getLogger(Cache.class);

    // the items are sorted by key.compare(key) -> we should map the String to a "CacheKey" which compares insertion time
    private final ConcurrentNavigableMap<String,T> items = new ConcurrentSkipListMap<String, T>();
    private final Queue<String> keys = new ConcurrentLinkedQueue<String>();
    private volatile int limit = 1000;
    private volatile long expiration = 30L * 60L * 1000L;
    private volatile long lastFlush = currentTime();
    private String name;
    public final static String PROPERTY_LIMIT_PREFIX = "oskari.cache.limit.";
    private boolean cacheSizeConfigured = false;
    private boolean cacheMissDebugEnabled = false;

    public void setCacheMissDebugEnabled(boolean enabled) {
        cacheMissDebugEnabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        // setName is called after constructor by CacheManager so get the limit from properties in here
        int configuredLimit = PropertyUtil.getOptional(getLimitPropertyName(), -1);
        if(configuredLimit != -1) {
            cacheSizeConfigured = true;
            limit = configuredLimit;
        }
    }

    private String getLimitPropertyName() {
        return PROPERTY_LIMIT_PREFIX + getName();
    }

    public int getLimit() {
        return limit;
    }

    /**
     * Amount of items to hold in cache. Defaults to 1000.
     * @param limit
     */
    public void setLimit(int limit) {
        if(cacheSizeConfigured) {
            log.info("Trying to set cache limit, but it's configured by user so ignoring automatic limit change.",
                    "Limit is", this.limit, "- Change limit with property: ", getLimitPropertyName());
            return;
        }
        this.limit = limit;
    }

    /**
     * Time between flushes to keep cached values
     * @return
     */
    public long getExpiration() {
        return expiration;
    }

    /**
     * Returns number of cached items
     * @return
     */
    public long getSize() {
        return items.size();
    }

    /**
     * Returns keys for cached items
     * @return
     */
    public Set<String> getKeys() {
        return items.keySet();
    }

    /**
     * Time to hold items in cache. Defaults to 30 minutes.
     * @param expiration in milliseconds
     */
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public long getLastFlush() {
        return lastFlush;
    }

    public T get(final String name) {
        flush(false);
        T value = items.get(name);

        if(cacheMissDebugEnabled && value == null) {
            log.debug("Cache", getName(), "miss for name", name);
        }
        return value;
    }

    public T remove(final String name) {
        flush(false);
        T value = items.remove(name);
        keys.remove(name);
        return value;
    }

    public boolean put(final String name, final T item) {
        flush(false);
        if(item == null) {
            // can't save null value
            return false;
        }
        final boolean overflowing = (items.size() >= limit);
        if(overflowing) {
            // limit reached - remove oldest object
            log.warn("Cache", getName(), "overflowing! Limit is", limit);
            log.info("Configure larger limit for cache by setting the property:", getLimitPropertyName());
            final String key = keys.poll();
            if(key != null) {
                items.remove(key);
            }
        }
        items.put(name, item);
        keys.add(name);
        return overflowing;
    }

    public boolean flush(final boolean force) {
        final long now = currentTime();
        if(force || isTimeToFlush(now)) {
            // flushCache
            log.debug("Flushing cache! Cache:", getName(), "Forced: ", force);
            items.clear();
            keys.clear();
            lastFlush = now;
            return true;
        }
        return false;
    }

    public boolean isTimeToFlush(long now) {
        return (lastFlush + expiration < now);
    }

    private static long currentTime() {
        return System.nanoTime() / 1000000L;
    }
}

package fi.nls.oskari.cache;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Simple generic in memory cache
 */
public class Cache<T> {

    private static final Logger log = LogFactory.getLogger(Cache.class);

    private ConcurrentSkipListMap<String,T> items = new ConcurrentSkipListMap<String, T>();
    private int limit = 1000;
    private long expiration = 30 * 60 * 1000;
    private long lastFlush = System.currentTimeMillis();
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLimit() {
        return limit;
    }

    /**
     * Amount of items to hold in cache. Defaults to 1000.
     * @param limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getExpiration() {
        return expiration;
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
        if(value == null) {
            log.debug("Cache", getName(), "miss for name", name);
        }
        return value;
    }

    public boolean put(final String name, final T item) {
        flush(false);
        boolean overflowing = false;
        if(items.size() >= limit) {
            // limit reached - remove oldest object
            log.debug("Cache", getName(),"overflowing! Limit is", limit);
            final String key = items.firstKey();
            items.remove(key);
            overflowing = true;
        }
        items.put(name, item);
        return overflowing;
    }

    public boolean flush(final boolean force) {
        final long now = System.currentTimeMillis();
        if(force || (lastFlush + expiration < now)) {
            // flushCache
            log.debug("Flushing cache! Forced: ", force);
            items.clear();
            lastFlush = now;
            return true;
        }
        return false;
    }
}

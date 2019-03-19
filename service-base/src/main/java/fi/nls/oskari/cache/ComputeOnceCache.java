package fi.nls.oskari.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ComputeOnceCache<T> extends Cache<T> {

    private static final long EXPIRATION_DEFAULT = TimeUnit.MINUTES.toMillis(30);

    private final ConcurrentHashMap<String, T> tmp;

    public ComputeOnceCache(int limit) {
        this(limit, EXPIRATION_DEFAULT);
    }

    public ComputeOnceCache(int limit, long expiration) {
        setLimit(limit);
        setExpiration(expiration);
        tmp = new ConcurrentHashMap<>();
    }

    public T get(final String key, final Function<String, T> mappingFunction) {
        T value = super.get(key);
        if (value != null) {
            return value;
        }

        final AtomicBoolean b = new AtomicBoolean(false);
        value = tmp.computeIfAbsent(key, (String k) -> {
            // Re-check the cache - maybe someone just completed this
            // and executed the if (b.get()) {}-block after we
            // had already finished the first cache.get(key) call;
            T val = super.get(k);
            if (val != null) {
                return val;
            }
            b.set(true);
            return mappingFunction.apply(k);
        });

        if (b.get()) {
            // I was the one to do the computation
            // Add the value to the actual cache
            super.put(key, value);
            // And remove the value from the computation map
            tmp.remove(key);
            // Do this after and not within the computeIfAbsent() call since
            // we're not supposed to modify the underlying map within computeIfAbsent()
        }

        return value;
    }

}

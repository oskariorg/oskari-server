package org.oskari.print.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<T> extends LinkedHashMap<String, T> {

    private static final long serialVersionUID = 1L;

    private final int limit;

    private LRUCache(final int limit) {
        super(64, 0.70f, true);
        this.limit = limit;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<String, T> eldest) {
        return size() > limit;
    }

    public static <T> Map<String, T> createLRUCache(int limit) {
        return Collections.synchronizedMap(new LRUCache<T>(limit));
    }

}
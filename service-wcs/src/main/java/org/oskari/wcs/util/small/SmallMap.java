package org.oskari.wcs.util.small;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Immutable Map<String, String> with linear searching If you need another
 * constructor you are probably doing it wrong
 */
public class SmallMap implements Map<String, String> {

    private final String[] k;
    private final String[] v;
    private final int len;
    private final AtomicReference<Set<Map.Entry<String, String>>> entrySet = new AtomicReference<>();

    public SmallMap(String k1, String v1) {
        len = 1;
        k = new String[] { k1 };
        assertKeysUnique();
        v = new String[] { v1 };
    }

    public SmallMap(String k1, String v1, String k2, String v2) {
        len = 2;
        k = new String[] { k1, k2 };
        assertKeysUnique();
        v = new String[] { v1, v2 };
    }

    public SmallMap(String k1, String v1, String k2, String v2, String k3, String v3) {
        len = 3;
        k = new String[] { k1, k2, k3 };
        assertKeysUnique();
        v = new String[] { v1, v2, v3 };
    }

    public SmallMap(String k1, String v1, String k2, String v2, String k3, String v3, String k4,
            String v4) {
        len = 4;
        k = new String[] { k1, k2, k3, k4 };
        assertKeysUnique();
        v = new String[] { v1, v2, v3, v4 };
    }

    public SmallMap(String k1, String v1, String k2, String v2, String k3, String v3, String k4,
            String v4, String k5, String v5) {
        len = 5;
        k = new String[] { k1, k2, k3, k4, k5 };
        assertKeysUnique();
        v = new String[] { v1, v2, v3, v4, v5 };
    }

    public SmallMap(String k1, String v1, String k2, String v2, String k3, String v3, String k4,
            String v4, String k5, String v5, String k6, String v6) {
        len = 6;
        k = new String[] { k1, k2, k3, k4, k5, k6 };
        assertKeysUnique();
        v = new String[] { v1, v2, v3, v4, v5, v6 };
    }

    private void assertKeysUnique() {
        for (int i = 0; i < len - 1; i++) {
            for (int j = i + 1; j < len; j++) {
                if (k[i].equals(k[j])) {
                    throw new IllegalArgumentException("Not unique");
                }
            }
        }
    }

    @Override
    public String get(Object key) {
        for (int i = 0; i < len; i++) {
            if (k[i].equals(key)) {
                return v[i];
            }
        }
        return null;
    }

    @Override
    public Set<String> keySet() {
        return new SmallSet<>(k);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Collection<String> values() {
        return Arrays.asList(v);
    }

    @Override
    public boolean containsKey(Object key) {
        for (int i = 0; i < len; i++) {
            if (k[i].equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < len; i++) {
            if (v[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        Set<Map.Entry<String, String>> lazy = entrySet.get();
        if (lazy == null) {
            SmallEntry[] arr = new SmallEntry[len];
            for (int i = 0; i < len; i++) {
                arr[i] = new SmallEntry(k[i], v[i]);
            }
            lazy = new SmallSet<Map.Entry<String, String>>(arr);
            if (!entrySet.compareAndSet(null, lazy)) {
                return entrySet.get();
            }
        }
        return lazy;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String put(String k, String v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(Object o) {
        throw new UnsupportedOperationException();
    }

}

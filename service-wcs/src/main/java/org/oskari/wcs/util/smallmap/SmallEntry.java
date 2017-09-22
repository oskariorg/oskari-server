package org.oskari.wcs.util.smallmap;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable Map.Entry<String, String> Not necessarily any smaller than any
 * other Entry<String, String> But used with SmallEntry and SmallMap classes
 * hence the name
 */
public class SmallEntry implements Map.Entry<String, String> {

    private final String k;
    private final String v;

    protected SmallEntry(String key, String value) {
        this.k = key;
        this.v = value;
    }

    @Override
    public String getKey() {
        return k;
    }

    @Override
    public String getValue() {
        return v;
    }

    @Override
    public String setValue(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SmallEntry)) {
            return false;
        }
        SmallEntry b = (SmallEntry) o;
        if (k == null) {
            if (b.k != null) {
                return false;
            }
            if (v == null) {
                return b.v == null;
            }
            return v.equals(b.v);
        }
        if (!k.equals(b.k)) {
            return false;
        }
        if (v == null) {
            return b.v == null;
        }
        return v.equals(b.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(k, v);
    }

    @Override
    public String toString() {
        return k + "=" + v;
    }

}

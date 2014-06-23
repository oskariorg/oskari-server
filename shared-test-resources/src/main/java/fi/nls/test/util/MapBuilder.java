package fi.nls.test.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple helper for building maps
 */
public class MapBuilder {
    private Map<String, String> params = new HashMap<String,String>();

    public static MapBuilder build() {
        return new MapBuilder();
    }
    public MapBuilder put(final String key, final String value) {
        params.put(key, value);
        return this;
    }

    public MapBuilder put(final String key, final long value) {
        params.put(key, "" + value);
        return this;
    }

    public MapBuilder put(Map<String, String> map) {
        params.putAll(map);
        return this;
    }

    public Map<String, String> done() {
        return params;
    }
}

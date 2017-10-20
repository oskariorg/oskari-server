package org.oskari.utils.common;

import java.util.LinkedHashMap;
import java.util.Map;

public class Maps {

    public static Map<String, String> of(String k1, String v1) {
        Map<String, String> map = new LinkedHashMap<>(2);
        map.put(k1, v1);
        return map;
    }

    public static Map<String, String> of(String k1, String v1, String k2, String v2) {
        Map<String, String> map = new LinkedHashMap<>(4);
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static Map<String, String> of(String... kvp) {
        int n = kvp.length;
        if (n % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of arguments");
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < n;) {
            String key = kvp[i++];
            String value = kvp[i++];
            map.put(key, value);
        }
        return map;
    }

}

package org.oskari.geojson;

import java.util.List;
import java.util.Map;

public class GeoJSONUtil {

    /*
    In GeoJSON the default geometry of a Feature is not part of the properties map
    But in SimpleFeature the default geometry is considered a regular attribute
    The unorthodox value specified here is used as the name of the attribute for the
    default geometry extracted from the GeoJSON 'geometry' field in order to
    minimize the risk of the name conflicting with an existing attribute name
     */
    public static final String DEFAULT_GEOMETRY_ATTRIBUTE_NAME = "_geometry";

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(List<Object> list, int i) {
        return (Map<String, Object>) list.get(i);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(Map<String, Object> map, String key) {
        return (List<Object>) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(List<Object> list, int i) {
        return (List<Object>) list.get(i);
    }

    public static String getString(Map<String, Object> map, String key) {
        return (String) map.get(key);
    }

    public static double getDouble(List<Object> list, int i) {
        return ((Number) list.get(i)).doubleValue();
    }

}

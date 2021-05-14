package org.oskari.geojson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class GeoJSONSchemaDetector {

    public static SimpleFeatureType getSchema(Map<String, Object> json, CoordinateReferenceSystem crs) {
        return getSchema(json, crs, false);
    }

    @SuppressWarnings("unchecked")
    public static SimpleFeatureType getSchema(Map<String, Object> json, CoordinateReferenceSystem crs, boolean ignoreGeometriesUnderProperties) {
        // FIXME: This creates a side-effect by modifying the input as well as returning the SimpleFeatureType.
        // Might cause problems later on...

        // Map feature.geometry fields to JTS Geometries
        replaceGeometry(json, GeoJSONReader2::toGeometry);

        if (ignoreGeometriesUnderProperties) {
            replaceMapProperties(json, maybeGeometry -> isGeometry(maybeGeometry) ? null : maybeGeometry);
        } else {
            replaceMapProperties(json, maybeGeometry -> propertyToGeometry(maybeGeometry).orElse(maybeGeometry));
        }

        Map<String, Class<?>> bindings = new HashMap<>();
        String type = GeoJSONUtil.getString(json, GeoJSON.TYPE);
        switch (type) {
        case GeoJSON.FEATURE_COLLECTION:
            List<Object> features = GeoJSONUtil.getList(json, GeoJSON.FEATURES);
            for (Object feature : features) {
                addAttributes((Map<String, Object>) feature, bindings);
            }
            break;
        case GeoJSON.FEATURE:
            addAttributes(json, bindings);
            break;
        default:
            throw new IllegalArgumentException("Invalid type");
        }

        if (bindings.isEmpty()) {
            // Empty FeatureCollection
            return null;
        }

        SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
        sftb.setName("FeatureType");
        sftb.setNamespaceURI("http://oskari.org");
        sftb.setDefaultGeometry(GeoJSONUtil.DEFAULT_GEOMETRY_ATTRIBUTE_NAME);
        sftb.setCRS(crs);

        for (Map.Entry<String, Class<?>> attribute : bindings.entrySet()) {
            String name = attribute.getKey();
            Class<?> cl = attribute.getValue();
            if (cl.isAssignableFrom(Geometry.class)) {
                sftb.add(name, cl, crs);
            } else {
                sftb.add(name, cl);
            }
        }

        return sftb.buildFeatureType();
    }

    /**
     * Try to convert Map<String, Object> representing GeoJSON Geometry to JTS Geometry
     * @param maybeGeometry JSON Object under that might be GeoJSON Geometry
     */
    private static Optional<Object> propertyToGeometry(Map<String, Object> maybeGeometry) {
        try {
            return Optional.of(GeoJSONReader2.toGeometry(maybeGeometry));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Check if Map<String, Object> is GeoJSON Geometry
     * @param maybeGeometry JSON object under that might be GeoJSON Geometry
     */
    private static boolean isGeometry(Map<String, Object> maybeGeometry) {
        try {
            GeoJSONReader2.toGeometry(maybeGeometry);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static void addAttributes(Map<String, Object> json,
            Map<String, Class<?>> bindings) {
        if (!GeoJSON.FEATURE.equals(GeoJSONUtil.getString(json, GeoJSON.TYPE))) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE);
        }

        Object geom = json.get(GeoJSON.GEOMETRY);
        if (geom != null) {
            String key = GeoJSONUtil.DEFAULT_GEOMETRY_ATTRIBUTE_NAME;
            Class<? extends Geometry> geometryType = (Class<? extends Geometry>) geom.getClass();
            Class<? extends Geometry> currentType = (Class<? extends Geometry>) bindings.get(key);
            Class<? extends Geometry> newType = getOverrideGeometryType(geometryType, currentType);
            if (newType != null) {
                bindings.put(key, newType);
            }
        }

        Map<String, Object> properties = GeoJSONUtil.getMap(json, GeoJSON.PROPERTIES);
        if (properties == null) {
            return;
        }
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            String key = e.getKey();
            if (GeoJSONUtil.DEFAULT_GEOMETRY_ATTRIBUTE_NAME.equals(key)) {
                continue;
            }
            Object value = e.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof Geometry) {
                Class<? extends Geometry> geometryType = (Class<? extends Geometry>) value.getClass();
                Class<? extends Geometry> currentType = (Class<? extends Geometry>) bindings.get(key);
                Class<? extends Geometry> newType = getOverrideGeometryType(geometryType, currentType);
                if (newType != null) {
                    bindings.put(key, newType);
                }
                continue;
            }
            Class<?> currentClass = value.getClass();
            Class<?> storedClass = bindings.get(key);
            if (storedClass == null) {
                bindings.put(key, currentClass);
                continue;
            }
            if (storedClass != currentClass) {
                Class<?> newClass = getOverrideType(currentClass, storedClass);
                if (newClass != null) {
                    bindings.put(key, newClass);
                }
            }
        }
    }

    private static Class<? extends Geometry> getOverrideGeometryType(
            Class<? extends Geometry> geometryType,
            Class<? extends Geometry> currentStoredType) {
        // Don't have anything yet - use what we got
        if (currentStoredType == null) {
            return geometryType;
        }
        // Already at GeometryCollection - do nothing
        if (currentStoredType == GeometryCollection.class) {
            return currentStoredType;
        }
        // If they are equal do nothing - what's stored is good
        if (currentStoredType == geometryType) {
            return currentStoredType;
        }
        // Check if we have MultiPoint and the value is Point etc.
        if (isMultiVersionOf(currentStoredType, geometryType)) {
            // If so do nothing - what's stored is good
            return null;
        }
        // Check if the value is multi version of we currently have
        if (isMultiVersionOf(geometryType, currentStoredType)) {
            // Then use the multi version
            return geometryType;
        }
        // They weren't the same and they weren't compatible Multi* versions of each other
        // => Widen to Geometrycollection
        return GeometryCollection.class;
    }

    private static boolean isMultiVersionOf(
            Class<? extends Geometry> a,
            Class<? extends Geometry> b) {
        if (a == MultiPoint.class) {
            return b == Point.class;
        }
        if (a == MultiLineString.class) {
            return b == LineString.class;
        }
        if (a == MultiPolygon.class) {
            return b == Polygon.class;
        }
        return false;
    }

    // TODO: Improve me
    private static Class<?> getOverrideType(Class<?> currentClass, Class<?> storedClass) {
        boolean isCurrentNumber = currentClass.isAssignableFrom(Number.class);
        boolean isStoredNumber = storedClass.isAssignableFrom(Number.class);
        if (isCurrentNumber && isStoredNumber) {
            // Int and Double for example
            // => Just widen to Double
            if (storedClass == Double.class) {
                // But it's already Double so don't change anything
                return null;
            }
            return Double.class;
        }
        if (isStoredNumber) {
            // Stored is a number but current isn't
            // It's hopefully a String, let's use that
            return currentClass;
        }

        return null;
    }

    /**
     * Replaces feature.geometry fields that are currently of type Map<String, Object>
     */
    public static void replaceGeometry(Map<String, Object> geojson,
            Function<Map<String, Object>, Object> mapper) {
        String type = GeoJSONUtil.getString(geojson, GeoJSON.TYPE);
        if (type == null) {
            throw new IllegalArgumentException("Invalid GeoJSON object, missing 'type'");
        }
        switch (type) {
        case GeoJSON.FEATURE_COLLECTION:
            replaceGeometryFeatureCollection(geojson, mapper);
            break;
        case GeoJSON.FEATURE:
            replaceGeometryFeature(geojson, mapper);
            break;
        default:
            throw new IllegalArgumentException("Not GeoJSON FeatureCollection or Feature");
        }
    }

    /**
     * Replaces feature.properties values that are currently of type Map<String, Object>
     */
    public static void replaceMapProperties(Map<String, Object> geojson,
            Function<Map<String, Object>, Object> mapper) {
        String type = GeoJSONUtil.getString(geojson, GeoJSON.TYPE);
        if (type == null) {
            throw new IllegalArgumentException("Invalid GeoJSON object, missing 'type'");
        }
        switch (type) {
        case GeoJSON.FEATURE_COLLECTION:
            replaceMapPropertiesFeatureCollection(geojson, mapper);
            break;
        case GeoJSON.FEATURE:
            replaceMapPropertiesFeature(geojson, mapper);
            break;
        default:
            throw new IllegalArgumentException("Not GeoJSON FeatureCollection or Feature");
        }
    }

    private static void replaceGeometryFeatureCollection(Map<String, Object> featureCollection,
            Function<Map<String, Object>, Object> mapper) {
        String type = GeoJSONUtil.getString(featureCollection, GeoJSON.TYPE);
        if (!GeoJSON.FEATURE_COLLECTION.equals(type)) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE_COLLECTION);
        }

        List<Object> features = GeoJSONUtil.getList(featureCollection, GeoJSON.FEATURES);
        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> feature = GeoJSONUtil.getMap(features, i);
            replaceGeometryFeature(feature, mapper);
        }
    }

    @SuppressWarnings("unchecked")
    private static void replaceGeometryFeature(Map<String, Object> feature,
            Function<Map<String, Object>, Object> mapper) {
        String type = GeoJSONUtil.getString(feature, GeoJSON.TYPE);
        if (!GeoJSON.FEATURE.equals(type)) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE);
        }

        Object geometry = feature.get(GeoJSON.GEOMETRY);
        if (geometry != null && geometry instanceof Map) {
            geometry = mapper.apply((Map<String, Object>) geometry);
            feature.put(GeoJSON.GEOMETRY, geometry);
        }
    }

    private static void replaceMapPropertiesFeatureCollection(Map<String, Object> featureCollection,
            Function<Map<String, Object>, Object> mapper) {
        String type = GeoJSONUtil.getString(featureCollection, GeoJSON.TYPE);
        if (!GeoJSON.FEATURE_COLLECTION.equals(type)) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE_COLLECTION);
        }

        List<Object> features = GeoJSONUtil.getList(featureCollection, GeoJSON.FEATURES);
        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> feature = GeoJSONUtil.getMap(features, i);
            replaceMapPropertiesFeature(feature, mapper);
        }
    }

    @SuppressWarnings("unchecked")
    private static void replaceMapPropertiesFeature(Map<String, Object> feature,
            Function<Map<String, Object>, Object> mapper) {
        String type = GeoJSONUtil.getString(feature, GeoJSON.TYPE);
        if (!GeoJSON.FEATURE.equals(type)) {
            throw new IllegalArgumentException("type was not " + GeoJSON.FEATURE);
        }

        Map<String, Object> properties = GeoJSONUtil.getMap(feature, GeoJSON.PROPERTIES);
        if (properties == null) {
            return;
        }
        for (String key : properties.keySet()) {
            if (GeoJSONUtil.DEFAULT_GEOMETRY_ATTRIBUTE_NAME.equals(key)) {
                continue;
            }
            Object o = properties.get(key);
            if (o != null && o instanceof Map) {
                properties.put(key, mapper.apply((Map<String, Object>) o));
            }
        }
    }

}

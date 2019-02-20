package org.oskari.service.wfs.client.geojson;

import java.util.Map;
import java.util.UUID;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility class for converting a Map<String, Object> presentation
 * of a GeoJSON Feature to a SimpleFeature.
 * 
 * This class is package private by design, the use case for this
 * is rather specific. You really shouldn't be using this class in any other context.
 */
class MapToGeoJSONFeature {

    /**
     * Tries to convert a Map<String, Object> presentation of a GeoJSON
     * Feature to a SimpleFeature.
     * @param maybeFeature the possibly GeoJSON Feature
     * @return null if it can't be done, and the actual Feature if everything is OK
     */
    @SuppressWarnings("unchecked")
    static SimpleFeature tryConvertToSimpleFeature(Map<String, Object> maybeFeature) {
        try {
            // Don't handle nulls in this context, try to avoid cluttering the code
            // If something is null along the way then an NPE is thrown
            // but those are handled by catching all exceptions
            if (!"Feature".equals(maybeFeature.get("type"))) {
                throw new IllegalArgumentException("type must be 'Feature'");
            }
            if (!maybeFeature.containsKey("geometry")) {
                throw new IllegalArgumentException("Feature must have a member with the name 'geometry'");
            }
            if (!maybeFeature.containsKey("properties")) {
                throw new IllegalArgumentException("Feature must have a member with the name 'properties'");
            }

            Object maybeId = maybeFeature.get("id");
            String id;
            if (maybeId != null) {
                id = maybeId.toString();
            } else {
                id = UUID.randomUUID().toString();
            }

            Object maybeGeometry = maybeFeature.get("geometry");
            Geometry geometry = null;
            if (maybeGeometry != null) {
                geometry = MapToGeoJSONGeometry.tryConvertToGeometry((Map<String, Object>) maybeGeometry);
            }

            Map<String, Object> properties = (Map<String, Object>) maybeFeature.get("properties");

            return buildFeature(id, geometry, properties);
        } catch (Exception ignore) {
            // Something failed, probably a NPE somewhere along the way
            // But we don't really care _why_ it wasn't a proper GeoJSON geometry
            // the fact that it wasn't is good enough for us
        }
        return null;
    }

    private static SimpleFeature buildFeature(String id,
            Geometry geometry,
            Map<String, Object> properties) {
        SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();

        // If geometry is not null remove we disallow property with name "geometry"
        if (geometry != null) {
            properties.remove("geometry");
        }

        convertGeometryPropertiesToGeometry(properties);

        if (properties != null) {
            properties.forEach((k, v) -> {
                if (v != null) {
                    sftb.add(k, v.getClass());
                    // Set the "first" non-null Geometry property to be the defaultGeometry 
                    if (sftb.getDefaultGeometry() == null && v instanceof Geometry) {
                        sftb.setDefaultGeometry(k);
                    }
                }
            });
        }

        if (geometry != null) {
            sftb.add("geometry", geometry.getClass());
            // Override any potential defaultGeometry column
            sftb.setDefaultGeometry("geometry");
        }

        sftb.setName("Oskari generic SimpleFeatureType");

        SimpleFeatureType sft = sftb.buildFeatureType();

        SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(sft);
        if (properties != null) {
            properties.forEach((k, v) -> {
                if (v != null) {
                    sfb.set(k, v);
                }
            });
        }
        if (geometry != null) {
            sfb.set("geometry", geometry);
        }

        return sfb.buildFeature(id);
    }

    private static void convertGeometryPropertiesToGeometry(Map<String, Object> properties) {
        if (properties == null) {
            return;
        }
        for (String key : properties.keySet()) {
            Object value = properties.get(key);
            if (!(value instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> maybeGeometry = (Map<String, Object>) value;
            Geometry geom = MapToGeoJSONGeometry.tryConvertToGeometry(maybeGeometry);
            if (geom != null) {
                properties.put(key, geom);
            }
        }
    }

}

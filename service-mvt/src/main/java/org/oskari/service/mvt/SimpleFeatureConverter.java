package org.oskari.service.mvt;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.type.Name;

import java.math.BigDecimal;
import java.util.*;

public class SimpleFeatureConverter {

    private static final Logger LOG = LogFactory.getLogger(SimpleFeatureConverter.class);

    private static final String KEY_ID = "_oid";
    private static final String COMPLEX_PROP_PREFIX = "$";

    public static Optional<Feature> fromGeometry(Geometry geom) {
        if (geom == null || !(geom.getUserData() instanceof SimpleFeature)) {
            LOG.debug("userData not a SimpleFeature!");
            return Optional.empty();
        }
        Feature feature = new Feature();
        SimpleFeature f = (SimpleFeature) geom.getUserData();
        String id = f.getID();
        feature.id = id;
        feature.properties = new LinkedHashMap<>();
        feature.properties.put(KEY_ID, id);
        feature.geom = geom;

        Name geomPropertyName = f.getDefaultGeometryProperty().getName();
        for (Property p : f.getProperties()) {
            Name name = p.getName();
            if (geomPropertyName.equals(name)) {
                // Skip geometry
                continue;
            }
            String prop = name.getLocalPart();
            Object value = p.getValue();
            if (value == null) {
                LOG.debug("Skipping", id + "." + prop, "value is null");
                continue;
            }

            String mvtProp = convertPropertyNameToMVT(prop, value);
            Object mvtValue = convertValueToMVT(value);
            if (mvtValue == null) {
                LOG.debug("Skipping", id + "." + prop,
                        "could not handle class:", value.getClass());
                continue;
            }
            feature.properties.put(mvtProp, mvtValue);
        }
        return Optional.of(feature);
    }

    private static String convertPropertyNameToMVT(String prop, Object value) {
        if (value instanceof Map || value instanceof List) {
            return COMPLEX_PROP_PREFIX + prop;
        }
        return prop;
    }

    private static Object convertValueToMVT(Object value) {
        if (value instanceof Boolean
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Float
                || value instanceof Double
                || value instanceof String) {
            return value;
        }
        if (value instanceof Map) {
            return new JSONObject((Map) value).toString();
        }
        if (value instanceof List) {
            return new JSONArray((List) value).toString();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        // TODO: Handle dates and timestamps
        return null;
    }
}

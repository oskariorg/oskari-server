package org.oskari.service.mvt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;

import com.wdtinc.mapbox_vector_tile.VectorTile.Tile.Feature.Builder;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IUserDataConverter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SimpleFeatureConverter implements IUserDataConverter {

    private static final Logger LOG = LogFactory.getLogger(SimpleFeatureConverter.class);

    private static final String KEY_ID = "_oid";
    private static final String COMPLEX_PROP_PREFIX = "$";

    @Override
    public void addTags(Object userData, MvtLayerProps layerProps, Builder featureBuilder) {
        if (!(userData instanceof SimpleFeature)) {
            LOG.debug("userData not a SimpleFeature!");
            return;
        }
        SimpleFeature f = (SimpleFeature) userData;

        String id = f.getID();
        addId(layerProps, featureBuilder, id);

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

            int valueIndex = layerProps.addValue(mvtValue);
            if (valueIndex < 0) {
                // Value wasn't IN (Boolean,Integer,Long,Float,Double,String)
                // => Can't be encoded to MVT
                LOG.warn("Skipping", id + "." + prop,
                        "value type not valid for MVT encoding, class:", value.getClass());
                continue;
            }

            int keyIndex = layerProps.addKey(mvtProp);
            featureBuilder.addTags(keyIndex);
            featureBuilder.addTags(valueIndex);
        }
    }

    private String convertPropertyNameToMVT(String prop, Object value) {
        if (value instanceof Map || value instanceof List) {
            return COMPLEX_PROP_PREFIX + prop;
        }
        return prop;
    }

    private Object convertValueToMVT(Object value) {
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

    private void addId(MvtLayerProps layerProps, Builder featureBuilder, String id) {
        if (id == null || id.isEmpty()) {
            return;
        }
        int valueIndex = layerProps.addValue(id);
        int keyIndex = layerProps.addKey(KEY_ID);
        featureBuilder.addTags(keyIndex);
        featureBuilder.addTags(valueIndex);
    }

}

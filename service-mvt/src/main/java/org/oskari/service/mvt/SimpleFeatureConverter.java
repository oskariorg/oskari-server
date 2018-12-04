package org.oskari.service.mvt;

import java.math.BigDecimal;

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
                continue;
            }
            if (value instanceof BigDecimal) {
                value = ((BigDecimal) value).doubleValue();
            }

            int valueIndex = layerProps.addValue(value);
            if (valueIndex < 0) {
                // Value wasn't IN (Boolean,Integer,Long,Float,Double,String)
                // => Can't be encoded to MVT
                // TODO: Check how dates and datetimes are handled
                LOG.debug("Skipping", id + "." + prop,
                        "value type not valid for MVT encoding, class:", value.getClass());
                continue;
            }

            int keyIndex = layerProps.addKey(prop);
            featureBuilder.addTags(keyIndex);
            featureBuilder.addTags(valueIndex);
        }
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

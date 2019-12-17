package org.oskari.control.userlayer;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.control.userlayer.UserLayerWFSHelper;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.MultiLineString;

public class UserLayerWFSHelperTest {

    @Test
    public void testRetype() throws Exception {
        ObjectMapper om = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String,Object>>() {};
        Map<String, Object> geojson;
        try (InputStream in = getClass().getResourceAsStream("geojson.json")) {
            geojson = om.readValue(in, typeRef);
        }
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3067");
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs);
        SimpleFeatureCollection original = GeoJSONReader2.toFeatureCollection(geojson, schema);

        SimpleFeature f = null;
        SimpleFeatureCollection retyped = new UserLayerWFSHelper().postProcess(original);
        try (SimpleFeatureIterator it = retyped.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                if ("vuser_layer_data.2970316".equals(feature.getID())) {
                    f = feature;
                    break;
                }
            }
        }

        assertEquals(5, f.getAttributeCount());
        Property defaultGeometry = f.getDefaultGeometryProperty();
        Property interpoloi = f.getProperty("INTERPOLOI");
        Property lahdeainei = f.getProperty("LAHDEAINEI");
        Property id = f.getProperty("ID");
        Property laji = f.getProperty("LAJI");

        assertEquals(original.getSchema().getGeometryDescriptor().getName().getLocalPart(), defaultGeometry.getName().getLocalPart());
        assertEquals(MultiLineString.class, defaultGeometry.getType().getBinding());
        assertEquals(MultiLineString.class, defaultGeometry.getValue().getClass());

        assertEquals(Integer.class, interpoloi.getValue().getClass());
        assertEquals(1, interpoloi.getValue());

        assertEquals(String.class, lahdeainei.getValue().getClass());
        assertEquals("0", lahdeainei.getValue());

        assertEquals(Double.class, id.getValue().getClass());
        assertEquals(1.48627129E8, ((Double) id.getValue()).doubleValue(), 1e-8);

        assertEquals(String.class, laji.getValue().getClass());
        assertEquals("696", laji.getValue());
    }
}

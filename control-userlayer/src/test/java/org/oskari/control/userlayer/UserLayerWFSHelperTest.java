package org.oskari.control.userlayer;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.junit.Test;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.MultiLineString;

public class UserLayerWFSHelperTest {

    private ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Map<String, Object> readResource(String filename) throws Exception {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String,Object>>() {};
        try (InputStream in = getClass().getResourceAsStream(filename)) {
            return OBJECT_MAPPER.readValue(in, typeRef);
        }
    }

    private SimpleFeatureCollection createCollection(Map<String, Object> geojson) throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3067");
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs);
        SimpleFeatureCollection original = GeoJSONReader2.toFeatureCollection(geojson, schema);
        return original;
    }

    @Test
    public void testRetype() throws Exception {
        Map<String, Object> geojson = readResource("geojson.json");
        SimpleFeatureCollection original = createCollection(geojson);

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

    @Test
    public void testTooManyAttributesOnFeatures() throws Exception {
        Map<String, Object> geojson = readResource("geojson.json");
        // the first feature is used to define schema for whole collection on retype
        // remove an attribute from the _first_ feature to test if parsing fails
        // when following features have _more_ attributes than defined in schema
        dropAttrFromFirstFeature(geojson);
        SimpleFeatureCollection original = createCollection(geojson);

        SimpleFeatureCollection retyped = new UserLayerWFSHelper().postProcess(original);
        try (SimpleFeatureIterator it = retyped.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                assertEquals(4, feature.getAttributeCount());
            }
        }
    }

    @Test
    public void testMissingAttributesOnFeatures() throws Exception {
        Map<String, Object> geojson = readResource("geojson.json");
        // the first feature is used to define schema for whole collection on retype
        // add a attribute that is only on the first feature to test if parsing fails
        // when following features _don't_ have all attributes defined in schema
        addAttrToFirstFeature(geojson);
        SimpleFeatureCollection original = createCollection(geojson);

        SimpleFeatureCollection retyped = new UserLayerWFSHelper().postProcess(original);
        try (SimpleFeatureIterator it = retyped.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                assertEquals(6, feature.getAttributeCount());
            }
        }
    }

    private void dropAttrFromFirstFeature(Map<String, Object> geojson) throws Exception {
        List<Map> features = (List<Map>) geojson.get("features");
        Map<String, Object> firstFeature = features.get(0);
        Map<String, Object> attributes = (Map<String, Object>) firstFeature.get("properties");
        String userLayerAttributes = (String) attributes.get("property_json");
        JSONObject props = new JSONObject(userLayerAttributes);
        // other features will have _more_ attributes than schema defines
        props.remove("LAJI");
        attributes.put("property_json", props.toString());
    }

    private void addAttrToFirstFeature(Map<String, Object> geojson) throws Exception {
        List<Map> features = (List<Map>) geojson.get("features");
        Map<String, Object> firstFeature = features.get(0);
        Map<String, Object> attributes = (Map<String, Object>) firstFeature.get("properties");
        String userLayerAttributes = (String) attributes.get("property_json");
        JSONObject props = new JSONObject(userLayerAttributes);
        props.put("TESTING_SCHEMA", "other features will have less attributes than schema defines");
        attributes.put("property_json", props.toString());
    }
}

package org.oskari.control.userlayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiLineString;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

        Assertions.assertEquals(5, f.getAttributeCount());
        Property defaultGeometry = f.getDefaultGeometryProperty();
        Property interpoloi = f.getProperty("INTERPOLOI");
        Property lahdeainei = f.getProperty("LAHDEAINEI");
        Property id = f.getProperty("ID");
        Property laji = f.getProperty("LAJI");

        Assertions.assertEquals(original.getSchema().getGeometryDescriptor().getName().getLocalPart(), defaultGeometry.getName().getLocalPart());
        Assertions.assertEquals(MultiLineString.class, defaultGeometry.getType().getBinding());
        Assertions.assertEquals(MultiLineString.class, defaultGeometry.getValue().getClass());

        Assertions.assertEquals(Integer.class, interpoloi.getValue().getClass());
        Assertions.assertEquals(1, interpoloi.getValue());

        Assertions.assertEquals(String.class, lahdeainei.getValue().getClass());
        Assertions.assertEquals("0", lahdeainei.getValue());

        Assertions.assertEquals(BigDecimal.class, id.getValue().getClass());
        Assertions.assertEquals(1.48627129E8, ((BigDecimal) id.getValue()).doubleValue(), 1e-8);

        Assertions.assertEquals(String.class, laji.getValue().getClass());
        Assertions.assertEquals("696", laji.getValue());
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

        // Check that the first feature has the same number of attributes as schema
        SimpleFeature firstFeature = retyped.features().next();
        Assertions.assertEquals(4, firstFeature.getAttributeCount());
        Assertions.assertEquals(4, retyped.getSchema().getAttributeCount());
        try (SimpleFeatureIterator it = retyped.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                // skip the first one.
                if (feature.getID().equals(firstFeature.getID())) {
                    continue;
                }
                Assertions.assertEquals(5, feature.getAttributeCount());
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

        // Check that the first feature has the same number of attributes as schema
        SimpleFeature firstFeature = retyped.features().next();
        Assertions.assertEquals(6, firstFeature.getAttributeCount());
        Assertions.assertEquals(6, retyped.getSchema().getAttributeCount());
        try (SimpleFeatureIterator it = retyped.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                // skip the first one.
                if (feature.getID().equals(firstFeature.getID())) {
                    continue;
                }
                Assertions.assertEquals(5, feature.getAttributeCount());
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

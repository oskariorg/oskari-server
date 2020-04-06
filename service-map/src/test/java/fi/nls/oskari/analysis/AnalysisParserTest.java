package fi.nls.oskari.analysis;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class AnalysisParserTest {

    private String featureJSON = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPoint\",\"coordinates\":[[378829.7102134564,6677854.26666888]]},\"properties\":{},\"id\":\"analyseDrawLayer0\"}";
    private String featureCollectionJSON = "{\"features\":[],\"type\":\"FeatureCollection\"}";

    @Test
    public void testGetUserContentAnalysisInputId() {
        assertNull("Non-prefixed layer should not have user content id", AnalysisParser.getUserContentAnalysisInputId("548569"));
        assertEquals("Myplaces-prefixed layer should have user content id", "1", AnalysisParser.getUserContentAnalysisInputId("myplaces_1"));
        assertEquals("Userlayer-prefixed layer should have user content id", "5235", AnalysisParser.getUserContentAnalysisInputId("userlayer_5235"));
        assertEquals("Analysis-prefixed layer should have user content id", "347", AnalysisParser.getUserContentAnalysisInputId("analysis_347"));
        assertEquals("Analyzing an analysis layer should have user content id", "68568", AnalysisParser.getUserContentAnalysisInputId("analysis_347_68568"));
    }

    @Test
    public void testGetGeoJSONInput() throws Exception {
        String ID_PREFIX = "testing";
        assertNull("Null input should produce null as GeoJSON", AnalysisParser.getGeoJSONInput(null, ID_PREFIX));
        JSONObject input = new JSONObject();
        JSONArray features = new JSONArray();
        input.putOpt("features", features);
        assertTrue("Empty features array in input should produce empty feature collection", JSONHelper.isEqual(getFeatureCollection(), new JSONObject(AnalysisParser.getGeoJSONInput(input, "testing"))));

        // test with one feature
        JSONObject firstFeature = generateFeature(false);
        features.put(firstFeature);
        JSONObject testFeature1 = copyFeature(firstFeature, ID_PREFIX + ".0");
        assertTrue("Feature in input should produce feature collection with overridden id",
                JSONHelper.isEqual(getFeatureCollection(testFeature1), new JSONObject(AnalysisParser.getGeoJSONInput(input, ID_PREFIX))));

        // test with two features with one having "crs" key
        JSONObject secondFeature = generateFeature(true);
        JSONObject testFeature2 = copyFeature(secondFeature, ID_PREFIX + ".1");
        // getGeoJSONInput() removes crs key no matter the value
        testFeature2.remove("crs");
        features.put(secondFeature);
        assertTrue("Feature in input should produce feature collection with overridden id",
                JSONHelper.isEqual(getFeatureCollection(testFeature1, testFeature2), new JSONObject(AnalysisParser.getGeoJSONInput(input, ID_PREFIX))));
    }

    private JSONObject generateFeature(boolean addCRS) throws Exception {
        JSONObject feature = new JSONObject(featureJSON);
        if (!addCRS) {
            return feature;
        }
        feature.put("crs", "doesnt really matter, this should be removed by getGeoJSONInput()");
        return feature;
    }

    private JSONObject copyFeature(JSONObject feature, String newId) throws Exception {
        JSONObject newFeature = new JSONObject(feature.toString());
        newFeature.put("id", newId);
        return newFeature;
    }

    private JSONObject getFeatureCollection(JSONObject... features) throws Exception {
        JSONObject collection = new JSONObject(featureCollectionJSON);
        if (features.length == 0 ) {
            return collection;
        }
        JSONArray list = collection.getJSONArray("features");
        for(JSONObject f : features) {
            list.put(f);
        }
        return collection;
    }
}
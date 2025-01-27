package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class LayerJSONFormatterMYPLACESTest {
    private final static LayerJSONFormatterMYPLACES FORMATTER = new LayerJSONFormatterMYPLACES();
    private final static String SRS = "EPSG:4326";
    private final static String LANG = PropertyUtil.getDefaultLanguage();

    // Copied from DB (select attributes from oskari_maplayer where name = 'oskari:my_places';)
    private static final String ATTRIBUTES = "{\"data\":{\"filter\":{\"default\":[\"name\",\"place_desc\",\"image_url\",\"link\"],\"fi\":[\"name\",\"place_desc\",\"image_url\",\"link\"]},\"format\":{\"image_url\":{\"noLabel\":true,\"skipEmpty\":true,\"type\":\"image\",\"params\":{\"link\":true}},\"place_desc\":{\"noLabel\":true,\"skipEmpty\":true,\"type\":\"p\"},\"name\":{\"noLabel\":true,\"type\":\"h3\"},\"link\":{\"skipEmpty\":true,\"type\":\"link\"},\"attention_text\":{\"type\":\"hidden\"}},\"locale\":{\"fi\":{\"image_url\":\"Kuvalinkki\",\"place_desc\":\"Kuvaus\",\"name\":\"Nimi\",\"link\":\"Lisätiedot\",\"attention_text\":\"Teksti kartalla\"},\"sv\":{\"image_url\":\"Bild-URL\",\"place_desc\":\"Beskrivelse\",\"name\":\"Namn\",\"link\":\"Mera information\",\"attention_text\":\"Placera text på kartan\"},\"en\":{\"image_url\":\"Image URL\",\"place_desc\":\"Description\",\"name\":\"Name\",\"link\":\"More information\",\"attention_text\":\"Text on map\"}}},\"maxFeatures\":2000,\"namespaceURL\":\"http://www.oskari.org\"}";
    private static final String LOCALE = "{\"sv\":{\"name\":\"Mitt kartlager\"},\"en\":{\"name\":\"My map layer\"}}";

    private static final String PROP_NAME = "{" +
            "\"name\": \"name\"," +
            "\"type\": \"string\"," +
            "\"rawType\": \"String\"," +
            "\"label\": \"Name\"," +
            "\"format\": {\"noLabel\": true, \"type\":\"h3\"}" +
        "}";
    private static final String PROP_IMAGE = "{" +
            "\"name\": \"image_url\"," +
            "\"type\": \"string\"," +
            "\"rawType\": \"String\"," +
            "\"label\": \"Image URL\"," +
            "\"format\": {" +
                "\"noLabel\": true," +
                "\"skipEmpty\": true," +
                "\"type\": \"image\"," +
                "\"params\": {" +
                    "\"link\": true" +
                "}" +
            "}" +
        "}";

    private static final String[] SE_LABELS =  {"Namn", "Beskrivelse", "Bild-URL", "Mera information","Placera text på kartan", null};

    @Test
    public void parseDescribeLayer() throws JSONException {
        OskariLayer baseLayer = new OskariLayer();
        baseLayer.setInternal(true);
        baseLayer.setAttributes(new JSONObject(ATTRIBUTES));

        MyPlaceCategory layer = new MyPlaceCategory();
        JSONObject layerJson = FORMATTER.getJSON(baseLayer, layer, SRS, LANG);

        JSONObject describeLayer = layerJson.getJSONObject("describeLayer");
        JSONObject vectorStyle = describeLayer.getJSONArray("styles").getJSONObject(0);
        JSONObject styleDef = vectorStyle.getJSONObject("style");
        Assertions.assertTrue(JSONHelper.isEqual(styleDef.getJSONObject("featureStyle"), WFSLayerOptions.getDefaultOskariStyle()), "VectorStyle should have default feature style definitions");
        Assertions.assertEquals("default", vectorStyle.getString("id"), "VectorStyle should have id");
        Assertions.assertEquals("oskari", vectorStyle.getString("type"), "VectorStyle should have type");

        JSONArray properties = describeLayer.getJSONArray("properties");
        Assertions.assertEquals(6, properties.length(), "Properties should include all (also geometry and hidden)");
        Assertions.assertTrue(JSONHelper.isEqual(properties.getJSONObject(0), new JSONObject(PROP_NAME)), "Property should get parsed like DescribeLayer");
        Assertions.assertTrue(JSONHelper.isEqual(properties.getJSONObject(2), new JSONObject(PROP_IMAGE)), "Property should get parsed like DescribeLayer");
        Assertions.assertTrue(properties.getJSONObject(4).getBoolean("hidden"), "Attention text should be hidden and after visible props");
        Assertions.assertEquals("geometry", properties.getJSONObject(5).getString("type"), "Geometry is last");

        String coverage = describeLayer.optString("coverage", null);
        Assertions.assertNull(coverage, "Shouldn't have coverage WKT");

        JSONObject controlData = describeLayer.getJSONObject("controlData");
        Assertions.assertEquals("collection", controlData.getString("styleType"), "Should have style type");
        Assertions.assertEquals("vector", controlData.getString("renderMode"), "Should have default render mode");
        Assertions.assertEquals(-1, controlData.getInt("clusteringDistance"), "Shouldn't have clustering distance");
        Assertions.assertFalse(controlData.getBoolean("isDefault"));

        layer.setDefault(true);
        JSONObject seJson = FORMATTER.getJSON(baseLayer, layer, SRS, "sv");
        List <Map<String, Object>> props = JSONHelper.getArrayAsList(seJson.getJSONObject("describeLayer").getJSONArray("properties"));
        String[] labels = props.stream()
                .map(m -> (String) m.getOrDefault("label", null))
                .toArray(String[]::new);
        Assertions.assertArrayEquals(SE_LABELS, labels, "Labels should be localized");
        Assertions.assertTrue(seJson.getJSONObject("describeLayer").getJSONObject("controlData").getBoolean("isDefault"));
    }

    @Test
    public void testLocale() throws JSONException {
        OskariLayer baseLayer = new OskariLayer();
        baseLayer.setInternal(true);
        baseLayer.setLocale(new JSONObject(LOCALE));

        MyPlaceCategory layer = new MyPlaceCategory();
        JSONObject layerJson = FORMATTER.getJSON(baseLayer, layer, SRS, LANG);
        // Some MyPlaces layers have empty locale in DB for auto created default layer
        Assertions.assertEquals("My map layer", layerJson.getString("name"), "Should have localized name from baselayer");
        Assertions.assertTrue(JSONHelper.isEqual(baseLayer.getLocale(), layerJson.optJSONObject("locale")), "and locale");

        String name = "Edited my place";
        layer.setName(LANG, name);
        layerJson = FORMATTER.getJSON(baseLayer, layer, SRS, LANG);
        Assertions.assertEquals(name, layerJson.getString("name"), "Should have localized name for WFS layer type");
        JSONObject locale = JSONHelper.createJSONObject(LANG,JSONHelper.createJSONObject("name", name));
        Assertions.assertTrue(JSONHelper.isEqual(locale, layerJson.optJSONObject("locale")), "and locale");
    }
}

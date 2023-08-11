package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class LayerJSONFormatterMYPLACESTest {
    private final static LayerJSONFormatterMYPLACES FORMATTER = new LayerJSONFormatterMYPLACES();
    private final static String SRS = "EPSG:4326";
    private final static String LANG = PropertyUtil.getDefaultLanguage();

    // Copied from DB (select attributes from oskari_maplayer where name = 'oskari:my_places';)
    private static final String ATTRIBUTES = "{\"data\":{\"filter\":{\"default\":[\"name\",\"place_desc\",\"image_url\",\"link\"],\"fi\":[\"name\",\"place_desc\",\"image_url\",\"link\"]},\"format\":{\"image_url\":{\"noLabel\":true,\"skipEmpty\":true,\"type\":\"image\",\"params\":{\"link\":true}},\"place_desc\":{\"noLabel\":true,\"skipEmpty\":true,\"type\":\"p\"},\"name\":{\"noLabel\":true,\"type\":\"h3\"},\"link\":{\"skipEmpty\":true,\"type\":\"link\"},\"attention_text\":{\"type\":\"hidden\"}},\"locale\":{\"fi\":{\"image_url\":\"Kuvalinkki\",\"place_desc\":\"Kuvaus\",\"name\":\"Nimi\",\"link\":\"Lisätiedot\",\"attention_text\":\"Teksti kartalla\"},\"sv\":{\"image_url\":\"Bild-URL\",\"place_desc\":\"Beskrivelse\",\"name\":\"Namn\",\"link\":\"Mera information\",\"attention_text\":\"Placera text på kartan\"},\"en\":{\"image_url\":\"Image URL\",\"place_desc\":\"Description\",\"name\":\"Name\",\"link\":\"More information\",\"attention_text\":\"Text on map\"}}},\"maxFeatures\":2000,\"namespaceURL\":\"http://www.oskari.org\"}";

    private static final String NAME = "{" +
            "\"name\": \"name\"," +
            "\"type\": \"string\"," +
            "\"rawType\": \"String\"," +
            "\"label\": \"Name\"," +
            "\"format\": {\"noLabel\": true, \"type\":\"h3\"}" +
        "}";
    private static final String IMAGE = "{" +
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
        Assert.assertTrue("VectorStyle should have default feature style definitions",
                JSONHelper.isEqual(styleDef.getJSONObject("featureStyle"), WFSLayerOptions.getDefaultOskariStyle()));
        Assert.assertEquals("VectorStyle should have id", "default", vectorStyle.getString("id"));
        Assert.assertEquals("VectorStyle should have type", "oskari", vectorStyle.getString("type"));

        JSONArray properties = describeLayer.getJSONArray("properties");
        Assert.assertEquals("Properties should include all (also geometry and hidden)", 6, properties.length());
        Assert.assertTrue("Property should get parsed like DescribeLayer",
                JSONHelper.isEqual(properties.getJSONObject(0), new JSONObject(NAME)));
        Assert.assertTrue("Property should get parsed like DescribeLayer",
                JSONHelper.isEqual(properties.getJSONObject(2), new JSONObject(IMAGE)));
        Assert.assertTrue("Attention text should be hidden and after visible props",
                properties.getJSONObject(4).getBoolean("hidden"));
        Assert.assertEquals("Geometry is last", "geometry",
                properties.getJSONObject(5).getString("type"));

        String coverage = describeLayer.optString("coverage", null);
        Assert.assertNull("Shouldn't have coverage WKT", coverage);

        JSONObject controlData = describeLayer.getJSONObject("controlData");
        Assert.assertEquals("Should have style type", "collection", controlData.getString("styleType"));
        Assert.assertEquals("Should have default render mode", "vector", controlData.getString("renderMode"));
        Assert.assertEquals("Shouldn't have clustering distance", -1, controlData.getInt("clusteringDistance"));

        JSONObject seJson = FORMATTER.getJSON(baseLayer, layer, SRS, "sv");
        List <Map<String, Object>> props = JSONHelper.getArrayAsList(seJson.getJSONObject("describeLayer").getJSONArray("properties"));
        String[] labels = props.stream()
                .map(m -> (String) m.getOrDefault("label", null))
                .toArray(String[]::new);
        Assert.assertArrayEquals("Labels should be localized", SE_LABELS, labels);
    }
}

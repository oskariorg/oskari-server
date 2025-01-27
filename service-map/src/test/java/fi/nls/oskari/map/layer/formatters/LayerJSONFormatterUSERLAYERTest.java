package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LayerJSONFormatterUSERLAYERTest {
    private final static LayerJSONFormatterUSERLAYER FORMATTER_USERLAYER = new LayerJSONFormatterUSERLAYER();
    private final static String SRS = "EPSG:4326";
    private final static String LANG = PropertyUtil.getDefaultLanguage();
    private final static String NAMESPACE = "http://www.oskari.org";

    private static final String FIELDS = "[{" +
            "\"name\": \"the_geom\"," +
            "\"type\": \"MultiPolygon\"" +
        "}, {" +
            "\"name\": \"NAME\"," +
            "\"type\": \"String\"," +
            "\"locales\": {" +
                "\"en\": \"Name\"," +
                "\"fi\": \"Nimi\"" +
            "}" +
        "}, {" +
            "\"name\": \"CODE\"," +
            "\"type\": \"Long\","+
            "\"locales\": {" +
                "\"en\": \"Code\"," +
                "\"fi\": \"Koodi\"" +
        "}" +
    "}]";

    private static final String EXPECTED_1 = "{" +
            "\"name\": \"NAME\"," +
            "\"type\": \"string\"," +
            "\"rawType\": \"String\"," +
            "\"label\": \"Name\"" +
        "}";
    private static final String EXPECTED_2 = "{" +
            "\"name\": \"CODE\"," +
            "\"type\": \"number\"," +
            "\"rawType\": \"Long\"," +
            "\"label\": \"Code\"" +
        "}";
    private static final String WKT = "POLYGON ((1 2, 1 3, 1 4, 2 4, 3 4, 3 3, 3 2, 2 2, 1 2))";

    @Test
    public void parseAttributes() throws JSONException {
        JSONArray fields = new JSONArray(FIELDS);
        JSONObject attrJson = FORMATTER_USERLAYER.parseAttributes(fields);
        WFSLayerAttributes attr = new WFSLayerAttributes(attrJson);
        fields.remove(2);
        WFSLayerAttributes anotherAttr = new WFSLayerAttributes(FORMATTER_USERLAYER.parseAttributes(fields));
        Assertions.assertEquals(1, anotherAttr.getSelectedAttributes().size());

        List<String> selected = attr.getSelectedAttributes();
        Assertions.assertEquals(2, selected.size(), "2 attributes selected");
        Assertions.assertEquals("NAME", selected.get(0), "NAME should be first");
        Assertions.assertEquals("CODE", selected.get(1), "CODE should be second");

        JSONObject en = attr.getLocalization().get();
        Assertions.assertEquals(2, en.length());
        Assertions.assertEquals("Name", en.getString("NAME"));
        JSONObject fi = attr.getLocalization("fi").get();
        Assertions.assertEquals(2, fi.length());
        Assertions.assertEquals("Nimi", fi.getString("NAME"));

        JSONObject data = attr.getAttributes().getJSONObject("data");
        JSONObject types = data.optJSONObject("types");
        Assertions.assertEquals("string", types.getString("NAME"));
        Assertions.assertEquals("number", types.getString("CODE"));

        Assertions.assertEquals("the_geom", data.getString("geometryName"));
        Assertions.assertEquals("MultiPolygon", data.getString("geometryType"));
    }

    @Test
    public void parseDescribeLayer() throws JSONException {
        OskariLayer baseLayer = new OskariLayer();
        baseLayer.setInternal(true);
        WFSLayerOptions opts = new WFSLayerOptions(null);
        opts.setClusteringDistance(20);
        opts.setRenderMode("render");
        baseLayer.setOptions(opts.getOptions());

        UserLayer ulayer = new UserLayer();
        ulayer.setFields(new JSONArray(FIELDS));
        //WKT is stored as WGS84
        ulayer.setWkt(WKTHelper.getBBOX(1, 2, 3, 4));
        JSONObject layerJson = FORMATTER_USERLAYER.getJSON(baseLayer, ulayer, SRS, LANG);

        JSONObject describeLayer = layerJson.getJSONObject("describeLayer");
        JSONObject vectorStyle = describeLayer.getJSONArray("styles").getJSONObject(0);
        JSONObject styleDef = vectorStyle.getJSONObject("style");
        Assertions.assertTrue(JSONHelper.isEqual(styleDef.getJSONObject("featureStyle"), WFSLayerOptions.getDefaultOskariStyle()), "VectorStyle should have default feature style definitions");
        Assertions.assertEquals("default", vectorStyle.getString("id"), "VectorStyle should have id");
        Assertions.assertEquals("oskari", vectorStyle.getString("type"), "VectorStyle should have type");

        JSONArray properties = describeLayer.getJSONArray("properties");
        Assertions.assertEquals(3, properties.length(), "Properties should include all fields (also geometry)");
        Assertions.assertTrue(JSONHelper.isEqual(properties.getJSONObject(1), new JSONObject(EXPECTED_1)), "Field should get parsed like DescribeLayer");
        Assertions.assertTrue(JSONHelper.isEqual(properties.getJSONObject(2), new JSONObject(EXPECTED_2)), "Field should get parsed like DescribeLayer");

        String coverage = describeLayer.getString("coverage");
        // If test fails (interpolation) could be also check that WKT exists coverage.startsWith("POLYGON ((1 ")
        Assertions.assertEquals(WKT, coverage, "Should have coverage WKT");

        JSONObject controlData = describeLayer.getJSONObject("controlData");
        Assertions.assertEquals("area", controlData.getString("styleType"), "Should have style type");
        Assertions.assertEquals("render", controlData.getString("renderMode"), "Should have render mode");
        Assertions.assertEquals(20, controlData.getInt("clusteringDistance"), "Should have clustering distance");
    }

}

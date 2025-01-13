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
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals(1, anotherAttr.getSelectedAttributes().size());

        List<String> selected = attr.getSelectedAttributes();
        Assert.assertEquals("2 attributes selected", 2, selected.size());
        Assert.assertEquals("NAME should be first", "NAME", selected.get(0));
        Assert.assertEquals( "CODE should be second","CODE", selected.get(1));

        JSONObject en = attr.getLocalization().get();
        Assert.assertEquals(2, en.length());
        Assert.assertEquals("Name", en.getString("NAME"));
        JSONObject fi = attr.getLocalization("fi").get();
        Assert.assertEquals(2, fi.length());
        Assert.assertEquals("Nimi", fi.getString("NAME"));

        JSONObject data = attr.getAttributes().getJSONObject("data");
        JSONObject types = data.optJSONObject("types");
        Assert.assertEquals("string", types.getString("NAME"));
        Assert.assertEquals("number", types.getString("CODE"));

        Assert.assertEquals("the_geom", data.getString("geometryName"));
        Assert.assertEquals("MultiPolygon", data.getString("geometryType"));
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
        Assert.assertTrue("VectorStyle should have default feature style definitions",
                JSONHelper.isEqual(styleDef.getJSONObject("featureStyle"), WFSLayerOptions.getDefaultOskariStyle()));
        Assert.assertEquals("VectorStyle should have id", "default", vectorStyle.getString("id"));
        Assert.assertEquals("VectorStyle should have type", "oskari", vectorStyle.getString("type"));

        JSONArray properties = describeLayer.getJSONArray("properties");
        Assert.assertEquals("Properties should include all fields (also geometry)", 3, properties.length());
        Assert.assertTrue("Field should get parsed like DescribeLayer",
                JSONHelper.isEqual(properties.getJSONObject(1), new JSONObject(EXPECTED_1)));
        Assert.assertTrue("Field should get parsed like DescribeLayer",
                JSONHelper.isEqual(properties.getJSONObject(2), new JSONObject(EXPECTED_2)));

        String coverage = describeLayer.getString("coverage");
        // If test fails (interpolation) could be also check that WKT exists coverage.startsWith("POLYGON ((1 ")
        Assert.assertEquals("Should have coverage WKT", WKT, coverage);

        JSONObject controlData = describeLayer.getJSONObject("controlData");
        Assert.assertEquals("Should have style type", "area", controlData.getString("styleType"));
        Assert.assertEquals("Should have render mode", "render", controlData.getString("renderMode"));
        Assert.assertEquals("Should have clustering distance", 20, controlData.getInt("clusteringDistance"));
    }

}

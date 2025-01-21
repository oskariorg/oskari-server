package fi.nls.oskari.domain.map.wfs;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WFSLayerOptionsTest {
    String baseOptions = "{\n" +
            "   \"clusteringDistance\": 10,\n" +
            "   \"renderMode\": \"vector\",\n" +
            "   \"labelProperty\": \"name\",\n" +
            "}";

    String options = "{\n" +
            "   \"renderMode\": \"mvt\",\n" +
            "   \"styles\":{\n" +
            "       \"default\":{\n" +
            "           \"featureStyle\":{\n" +
            "               \"image\":{\n" +
            "                   \"shape\":\"1\",\n" +
            "                   \"size\":5,\n" +
            "                   \"fill\":{\n" +
            "                       \"color\":\"#000000\"\n" +
            "                   }\n" +
            "               },\n" +
            "               \"fill\":{\n" +
            "                   \"area\":{\n" +
            "                       \"pattern\":3\n" +
            "                    },\n" +
            "                   \"color\":\"#000000\"\n" +
            "               },\n" +
            "               \"stroke\":{\n" +
            "                   \"area\":{\n" +
            "                       \"color\":\"#000000\",\n" +
            "                       \"lineDash\":\"solid\",\n" +
            "                       \"width\":1,\n" +
            "                       \"lineJoin\":\"mitre\"\n" +
            "                   },\n" +
            "                   \"color\":\"#3233ff\",\n" +
            "                   \"lineCap\": \"butt\",\n" +
            "                   \"lineDash\":\"solid\",\n" +
            "                   \"width\":1,\n" +
            "                   \"lineJoin\":\"mitre\"\n" +
            "               }\n" +
            "           }\n" +
            "       }\n" +
            "   }\n" +
            "}";

    String customStyle = "{\n" +
            "   \"fill\":{\n" +
            "       \"color\":\"#652d90\"\n" +
            "   }\n" +
            "}";

    @Test
    public void testDefaultOptions() throws JSONException {
        WFSLayerOptions opts = new WFSLayerOptions();
        Assertions.assertTrue(opts.getClusteringDistance() == -1, "No clustering by default");
        Assertions.assertEquals("vector", opts.getRenderMode(), "Default render mode should be vector");

        JSONObject style = opts.getDefaultFeatureStyle();
        Assertions.assertTrue(style.has("image"), "default style should have image");
        Assertions.assertTrue(style.has("fill"), "default style should have fill");
        Assertions.assertTrue(style.has("stroke"), "default style should have stroke");
    }
    @Test
    public void testSetOptions () throws JSONException {
        JSONObject input = new JSONObject(options);
        WFSLayerOptions opts = new WFSLayerOptions(input);
        Assertions.assertEquals("mvt", opts.getRenderMode());
        JSONObject style = opts.getDefaultFeatureStyle();
        Assertions.assertEquals(3, style.getJSONObject("fill").getJSONObject("area").getInt("pattern"));
    }
    @Test
    public void testOverrideDefaultStyle () throws JSONException {
        WFSLayerOptions opts = new WFSLayerOptions();
        opts.setDefaultFeatureStyle(new JSONObject(customStyle));
        JSONObject style = opts.getDefaultFeatureStyle();
        JSONObject fill = style.getJSONObject("fill");
        Assertions.assertEquals("#652d90", fill.getString("color"), "color");
    }
    @Test
    public void testBaseOptions () throws JSONException {
        JSONObject input = new JSONObject(options);
        WFSLayerOptions opts = new WFSLayerOptions(input);
        Assertions.assertEquals(-1, opts.getClusteringDistance());
        JSONObject baseOpts = new JSONObject(baseOptions);
        opts.injectBaseLayerOptions(baseOpts);
        Assertions.assertEquals("mvt", opts.getRenderMode(), "base options shouldn't override layer options");
        Assertions.assertEquals(10, opts.getClusteringDistance(), "clustering distance should be set from base options");
        JSONObject style = opts.getDefaultFeatureStyle();
        Assertions.assertTrue(style.has("text"), "options has labelProperty. So text style should be added");
        Assertions.assertEquals("name", style.getJSONObject("text").getString("labelProperty"));
    }
}

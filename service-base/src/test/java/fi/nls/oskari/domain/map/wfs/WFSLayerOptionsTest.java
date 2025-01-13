package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

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
        assertTrue("No clustering by default", opts.getClusteringDistance() == -1);
        assertEquals("Default render mode should be vector", "vector", opts.getRenderMode());

        JSONObject style = opts.getDefaultFeatureStyle();
        assertTrue("default style should have image", style.has("image"));
        assertTrue("default style should have fill", style.has("fill"));
        assertTrue("default style should have stroke", style.has("stroke"));
    }
    @Test
    public void testSetOptions () throws JSONException {
        JSONObject input = new JSONObject(options);
        WFSLayerOptions opts = new WFSLayerOptions(input);
        assertEquals("mvt", opts.getRenderMode());
        JSONObject style = opts.getDefaultFeatureStyle();
        assertEquals(3, style.getJSONObject("fill").getJSONObject("area").getInt("pattern"));
    }
    @Test
    public void testOverrideDefaultStyle () throws JSONException {
        WFSLayerOptions opts = new WFSLayerOptions();
        opts.setDefaultFeatureStyle(new JSONObject(customStyle));
        JSONObject style = opts.getDefaultFeatureStyle();
        JSONObject fill = style.getJSONObject("fill");
        assertEquals("color", "#652d90", fill.getString("color"));
    }
    @Test
    public void testBaseOptions () throws JSONException {
        JSONObject input = new JSONObject(options);
        WFSLayerOptions opts = new WFSLayerOptions(input);
        assertEquals(-1, opts.getClusteringDistance());
        JSONObject baseOpts = new JSONObject(baseOptions);
        opts.injectBaseLayerOptions(baseOpts);
        assertEquals("base options shouldn't override layer options", "mvt", opts.getRenderMode());
        assertEquals("clustering distance should be set from base options", 10, opts.getClusteringDistance());
        JSONObject style = opts.getDefaultFeatureStyle();
        assertTrue("options has labelProperty. So text style should be added", style.has("text"));
        assertEquals("name", style.getJSONObject("text").getString("labelProperty"));
    }
}

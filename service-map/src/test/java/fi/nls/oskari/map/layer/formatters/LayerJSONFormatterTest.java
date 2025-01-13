package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.capabilities.CapabilitiesConstants;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class LayerJSONFormatterTest {
    private final static LayerJSONFormatter FORMATTER = new LayerJSONFormatter();
    private final static String LANG = "en";
    private final static String CRS = "EPSG:4326";
    private final static String GLOBAL_LEGEND = "https://mydomain.org/global";
    private final static String LEGENDS = "{\"style1\":\"https://mydomain.org\"}";
    private final static String STYLES = "[{\"legend\":\"http://example.com/style1\",\"name\":\"style1\",\"title\":\"Style 1\"},{\"legend\":\"http://example.com/style2\",\"name\":\"style2\",\"title\":\"Style 2\"}]";

    @BeforeClass
    public static void addProperties() throws Exception {
        PropertyUtil.addProperty(LayerJSONFormatter.PROPERTY_AJAXURL, "action");
    }
    @AfterClass
    public static void teardown() {
        PropertyUtil.clearProperties();
    }
    private static OskariLayer initLayer (String type) throws  Exception {
        OskariLayer layer = new OskariLayer();
        layer.setType(type);
        JSONHelper.putValue(layer.getCapabilities(), CapabilitiesConstants.KEY_STYLES, new JSONArray(STYLES));
        JSONHelper.putValue(layer.getOptions(), LayerJSONFormatter.KEY_LEGENDS, new JSONObject(LEGENDS));
        return layer;
    }

    private static String getLegend (JSONObject layer, String styleName) throws Exception {
        JSONArray styles = layer.getJSONArray("styles");
        for (int i = 0; styles.length() > i; i++ ) {
            JSONObject style = styles.getJSONObject(i);
            String name = style.getString("name");
            if (styleName.equals(name)) {
                return style.optString("legend");
            }
        }
        return "";
    }
    private static void setGlobalLegend (OskariLayer layer) throws JSONException {
        layer.getOptions().getJSONObject(LayerJSONFormatter.KEY_LEGENDS).put(LayerJSONFormatter.KEY_GLOBAL_LEGEND, GLOBAL_LEGEND);
    }

    @Test
    public void legendWMS() throws Exception {
        OskariLayer layer = initLayer(OskariLayer.TYPE_WMS);
        JSONObject layerJSON = FORMATTER.getJSON(layer, LANG, false, CRS);
        Assert.assertEquals("Style 1 should have overrided legend url", "https://mydomain.org", getLegend(layerJSON, "style1"));
        Assert.assertEquals("Style 2 should have legend url defined in capabilities", "http://example.com/style2", getLegend(layerJSON, "style2"));


        setGlobalLegend(layer);
        layerJSON = FORMATTER.getJSON(layer, LANG, false, CRS);
        Assert.assertEquals("Style 1 should have overrided legend url", "https://mydomain.org", getLegend(layerJSON, "style1"));
        Assert.assertEquals("Style 2 should have global legend", GLOBAL_LEGEND, getLegend(layerJSON, "style2"));

        layer.setCapabilities(new JSONObject());
        layerJSON = FORMATTER.getJSON(layer, LANG, false, CRS);
        Assert.assertTrue(layerJSON.getJSONArray("styles").length() == 1);
        Assert.assertEquals("layer should have default style with global legend", GLOBAL_LEGEND, getLegend(layerJSON, ""));

    }
    @Test
    public void legendWMTS() throws Exception {
        OskariLayer layer = new OskariLayer();
        layer.setType(OskariLayer.TYPE_WMTS);
        JSONArray styles = new JSONArray("[{\"default\": true, \"legend\": null, \"name\": \"default\", \"title\": \"default\"}]");
        JSONHelper.putValue(layer.getCapabilities(), CapabilitiesConstants.KEY_STYLES, styles);

        JSONObject layerJSON = FORMATTER.getJSON(layer, LANG, true, CRS);
        JSONArray stylesJSON = layerJSON.getJSONArray("styles");
        Assert.assertEquals(1, stylesJSON.length());
        JSONObject styleJSON = stylesJSON.getJSONObject(0);
        Assert.assertEquals("Style should have name",  "default", styleJSON.getString("name"));
        Assert.assertEquals("Style should have title",  "default", styleJSON.getString("title"));
        Assert.assertTrue("Style shouldn't have legend url", styleJSON.getString("legend").isEmpty());
    }
    @Test
    public void proxyLegend() throws Exception {
        String proxyLegend = "action?action_route=GetLayerTile&legend=true&style=%s&id=-1";

        OskariLayer layer = initLayer(OskariLayer.TYPE_WMS);
        layer.setUsername("user");
        layer.setPassword("pass");
        JSONObject layerJSON = FORMATTER.getJSON(layer, LANG, true, CRS);
        Assert.assertEquals( String.format(proxyLegend, "style1"), getLegend(layerJSON, "style1"));

        layer = initLayer(OskariLayer.TYPE_WMS);
        JSONHelper.putValue(layer.getAttributes(), "forceProxy", true);
        layerJSON = FORMATTER.getJSON(layer, LANG, true, CRS);
        Assert.assertEquals( String.format(proxyLegend, "style1"), getLegend(layerJSON, "style1"));

        layer = initLayer(OskariLayer.TYPE_WMS);
        layerJSON = FORMATTER.getJSON(layer, LANG, true, CRS);
        Assert.assertEquals( "Don't proxy secure urls","https://mydomain.org", getLegend(layerJSON, "style1"));
        Assert.assertEquals( "Proxy non-secure url with secure connection",
                String.format(proxyLegend, "style2"),
                getLegend(layerJSON, "style2"));
    }

    // test deprecated methods
    @Test
    public void legendImage() throws Exception {
        OskariLayer layer = new OskariLayer();
        layer.setLegendImage(GLOBAL_LEGEND);
        Assert.assertEquals(GLOBAL_LEGEND,layer.getLegendImage());
    }
}

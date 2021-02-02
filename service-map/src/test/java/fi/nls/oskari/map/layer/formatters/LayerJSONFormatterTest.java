package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.capabilities.CapabilitiesConstants;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
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

    @Test
    public void getFixedDataUrl() {
        assertNull("Null metadata returns null id", LayerJSONFormatter.getFixedDataUrl(null));
        assertNull("Empty metadata returns null id", LayerJSONFormatter.getFixedDataUrl(""));
        assertEquals("Non http-starting metadata returns as is", "testing", LayerJSONFormatter.getFixedDataUrl("testing"));
        assertNull("Url without querystring returns null id", LayerJSONFormatter.getFixedDataUrl("http://mydomain.org"));
        assertNull("Url without id|uuid returns null id", LayerJSONFormatter.getFixedDataUrl("http://mydomain.org?my=key"));
        assertEquals("Url with id returns id value simple", "key", LayerJSONFormatter.getFixedDataUrl("http://mydomain.org?uuid=key"));
        Map<String, String> expected = new HashMap<>();
        expected.put("http://mydomain.org?test=test&id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuid=key2&post=test", "key2");
        expected.put("http://mydomain.org?test=test&Id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuId=Key&post=test", "Key");
        for (String url : expected.keySet()) {
            assertEquals("Url with id returns id value", expected.get(url), LayerJSONFormatter.getFixedDataUrl(url));
        }
    }
    @Test
    public void legendWMS() throws Exception {
        OskariLayer layer = initLayer(OskariLayer.TYPE_WMS);
        JSONObject layerJSON = FORMATTER.getJSON(layer, LANG, false, CRS);
        Assert.assertEquals("Style 1 should have overrided legend url", "https://mydomain.org", getLegend(layerJSON, "style1"));
        Assert.assertEquals("Style 2 should have legend url defined in capabilities", "http://example.com/style2", getLegend(layerJSON, "style2"));


        layer.setLegendImage(GLOBAL_LEGEND);
        layerJSON = FORMATTER.getJSON(layer, LANG, false, CRS);
        Assert.assertEquals("Style 1 should have overrided legend url", "https://mydomain.org", getLegend(layerJSON, "style1"));
        Assert.assertEquals("Style 2 should have global legend", GLOBAL_LEGEND, getLegend(layerJSON, "style2"));

        layer.setCapabilities(new JSONObject());
        layerJSON = FORMATTER.getJSON(layer, LANG, false, CRS);
        Assert.assertTrue(layerJSON.getJSONArray("styles").length() == 1);
        Assert.assertEquals("layer should have default style with global legend", GLOBAL_LEGEND, getLegend(layerJSON, "default"));

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
}

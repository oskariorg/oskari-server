package fi.nls.oskari.domain.map.wms;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SMAKINEN
 */
public class OskariLayerTest {

    @AfterEach
    public void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testSimplifiedWmsURL() {
        OskariLayer layer = new OskariLayer();
        Assertions.assertEquals("", layer.getSimplifiedUrl(), "Simplified wms url should be empty if wms url is not set");

        layer.setUrl(null);
        Assertions.assertEquals("", layer.getSimplifiedUrl(), "Simplified wms url should be empty if wms url is null");

        layer.setUrl("");
        Assertions.assertEquals("", layer.getSimplifiedUrl(), "Simplified wms url should be empty if wms url is empty");

        // key is wms url, value is simplified version
        final Map<String, String> tests = new HashMap<String, String>();
        // clean url
        tests.put("paikkatietoikkuna.fi", "paikkatietoikkuna.fi");
        // comma-separated
        tests.put("paikkatietoikkuna.fi, nls.fi", "paikkatietoikkuna.fi");

        // comma-separated with http-protocol
        tests.put("http://paikkatietoikkuna.fi, nls.fi", "paikkatietoikkuna.fi");
        // comma-separated with https-protocol
        tests.put("https://paikkatietoikkuna.fi, http://nls.fi", "paikkatietoikkuna.fi");

        // numbers and stuff
        tests.put("http://a.3.mydomain.fi,http://r2-d2-mydomain.fi", "a.3.mydomain.fi");

        // some weird looking propably wouldn't-work-anyway url
        tests.put("http://r2-d2-mydomain.fi , , : http://a.3.mydomain.fi", "r2-d2-mydomain.fi");

        // trimming
        tests.put(" paikkatietoikkuna.fi ", "paikkatietoikkuna.fi");

        for(String wmsurl : tests.keySet()) {
            layer.setUrl(wmsurl);
            Assertions.assertEquals(tests.get(wmsurl), layer.getSimplifiedUrl(), "Simplified wms url should be '" + tests.get(wmsurl) + "' if wms url is '" + wmsurl + "'");
        }
    }

    @Test
    public void testGetURL() throws Exception {
        PropertyUtil.addProperty("maplayer.wmsurl.secure", "https://", true);
        OskariLayer layer = new OskariLayer();
        final String url = "http://oskari.org";

        layer.setUrl(url);
        Assertions.assertEquals(url, layer.getUrl(), "Url should be '" + url + "' if url is '" + url + "'");
        Assertions.assertEquals("https://oskari.org", layer.getUrl(true), "Secure url should be 'https://oskari.org' if url is '" + url + "'");

        PropertyUtil.addProperty("maplayer.wmsurl.secure", "/tiles/", true);
        layer = new OskariLayer();
        layer.setUrl(url);
        Assertions.assertEquals(url, layer.getUrl(), "Url should be '" + url + "' if url is '" + url + "'");
        Assertions.assertEquals("/tiles/oskari.org", layer.getUrl(true), "Secure url should be '/tiles/oskari.org' if url is '" + url + "'");

        PropertyUtil.addProperty("maplayer.wmsurl.secure", "", true);
        PropertyUtil.addProperty("oskari.ajax.url.prefix", "/action?", true);
        layer = new OskariLayer();
        layer.setUrl(url);
        layer.setId(37);
        final String proxyUrl = "/action?action_route=GetLayerTile&id=37";
        Assertions.assertEquals(url, layer.getUrl(), "Url should be '" + url + "' if url is '" + url + "'");
        Assertions.assertEquals(proxyUrl, layer.getUrl(true), "Secure url should be '" + proxyUrl + "' if url is '" + url + "'");

    }
}

package fi.nls.oskari.domain.map.wms;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author SMAKINEN
 */
public class OskariLayerTest {

    @After
    public void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testSimplifiedWmsURL() {
        OskariLayer layer = new OskariLayer();
        assertEquals("Simplified wms url should be empty if wms url is not set", "", layer.getSimplifiedUrl());

        layer.setUrl(null);
        assertEquals("Simplified wms url should be empty if wms url is null", "", layer.getSimplifiedUrl());

        layer.setUrl("");
        assertEquals("Simplified wms url should be empty if wms url is empty", "", layer.getSimplifiedUrl());

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
            assertEquals("Simplified wms url should be '" + tests.get(wmsurl) + "' if wms url is '" + wmsurl + "'", tests.get(wmsurl), layer.getSimplifiedUrl());
        }
    }

    @Test
    public void testGetURL() throws Exception {
        PropertyUtil.addProperty("maplayer.wmsurl.secure", "https://", true);
        OskariLayer layer = new OskariLayer();
        final String url = "http://oskari.org";

        layer.setUrl(url);
        assertEquals("Url should be '" + url + "' if url is '" + url + "'", url, layer.getUrl());
        assertEquals("Secure url should be 'https://oskari.org' if url is '" + url + "'", "https://oskari.org", layer.getUrl(true));

        PropertyUtil.addProperty("maplayer.wmsurl.secure", "/tiles/", true);
        layer = new OskariLayer();
        layer.setUrl(url);
        assertEquals("Url should be '" + url + "' if url is '" + url + "'", url, layer.getUrl());
        assertEquals("Secure url should be '/tiles/oskari.org' if url is '" + url + "'", "/tiles/oskari.org", layer.getUrl(true));

        PropertyUtil.addProperty("maplayer.wmsurl.secure", "", true);
        PropertyUtil.addProperty("oskari.ajax.url.prefix", "/action?", true);
        layer = new OskariLayer();
        layer.setUrl(url);
        layer.setId(37);
        final String proxyUrl = "/action?action_route=GetLayerTile&id=37";
        assertEquals("Url should be '" + url + "' if url is '" + url + "'", url, layer.getUrl());
        assertEquals("Secure url should be '" + proxyUrl + "' if url is '" + url + "'", proxyUrl, layer.getUrl(true));

    }
}

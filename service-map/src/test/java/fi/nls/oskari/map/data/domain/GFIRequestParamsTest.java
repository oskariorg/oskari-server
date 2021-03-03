package fi.nls.oskari.map.data.domain;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

public class GFIRequestParamsTest {
    private static final String JSON = "application/json";
    private static final String TEXT = "text/plain";
    private static final String HTML = "text/html";

    private GFIRequestParams getParams () {
        GFIRequestParams params = new GFIRequestParams();
        OskariLayer layer = new OskariLayer();
        layer.setName("test");
        layer.setUrl("https://example.com/");
        params.setLayer(layer);
        return params;
    }
    private void setCapabilities (GFIRequestParams params, boolean setValue) {
        JSONObject capa = new JSONObject();
        JSONObject formats = new JSONObject();
        if (setValue) {
            JSONHelper.putValue(formats, KEY_VALUE, JSON);
        }
        JSONHelper.put(formats, KEY_AVAILABLE, new JSONArray(Arrays.asList(JSON, TEXT)));
        JSONHelper.putValue(capa, KEY_FORMATS, formats);
        params.getLayer().setCapabilities(capa);
    }
    private String parseInfoFormat(String url) {
        return IOHelper.parseQuerystring(url).get("INFO_FORMAT").get(0);
    }
    @Test
    public void testDefault() {
        String format = parseInfoFormat(getParams().getGFIUrl());
        Assert.assertEquals(HTML, format);
    }
    @Test
    public void testGFIType() {
        GFIRequestParams params = getParams();
        params.getLayer().setGfiType(JSON);
        String format = parseInfoFormat(params.getGFIUrl());
        Assert.assertEquals(JSON, format);
    }
    @Test
    public void testCapabilitiesValue() {
        GFIRequestParams params = getParams();
        setCapabilities(params, true);
        String format = parseInfoFormat(params.getGFIUrl());
        Assert.assertEquals(JSON, format);
    }
    @Test
    public void testCapabilitiesAvailable() {
        GFIRequestParams params = getParams();
        setCapabilities(params, false);
        String format = parseInfoFormat(params.getGFIUrl());
        Assert.assertEquals(TEXT, format);

    }
}

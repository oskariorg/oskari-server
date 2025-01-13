package fi.nls.oskari.map.data.service;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.data.domain.GFIRequestParams;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;

/**
 * Created by SMAKINEN on 24.11.2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(GetGeoPointDataService.class)
// these are needed with PowerMock and Java 11. Haven't tried if Java 13+ still needs these:
// https://github.com/powermock/powermock/issues/864
@PowerMockIgnore({"com.sun.org.apache.xalan.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.w3c.dom.*", "org.xml.*", "com.sun.org.apache.xml.*"})
public class GetGeoPointDataServiceTest {

    @Test
    public void testTransformResponse()
            throws IOException {
        final String xslt = IOHelper.readString(this.getClass().getResourceAsStream("iceland.xsl"));
        final String xml = IOHelper.readString(this.getClass().getResourceAsStream("iceland.xml"));
        GetGeoPointDataService service = new GetGeoPointDataService();
        assertNotNull(xslt);
        assertNotNull(xml);
        String response = service.transformResponse(xslt, xml);
        JSONObject json = JSONHelper.createJSONObject(response);
        assertNotNull(json);
        JSONObject parsed = json.optJSONObject("parsed");
        assertEquals("Should have html as Mynd", parsed.optString("Mynd"), "<a target='_blank' href='http://this-is-not-relevant'>link</a>");
        assertNotNull(parsed.optString("Staður"));

    }

    @Test
    public void testLuontoTransformResponse()
            throws IOException {
        final String xslt = IOHelper.readString(this.getClass().getResourceAsStream("luonto_gfi.xsl"));
        final String xml = IOHelper.readString(this.getClass().getResourceAsStream("luonto_gfi.xml"));
        GetGeoPointDataService service = new GetGeoPointDataService();
        assertNotNull(xslt);
        assertNotNull(xml);
        String response = service.transformResponse(xslt, xml);
        JSONObject json = JSONHelper.createJSONObject(response);
        assertNotNull(json);
        final String expected = IOHelper.readString(this.getClass().getResourceAsStream("luonto_gfi_expected.json"));
        // System.out.println(expected);
        assertTrue("Should match expected", JSONHelper.isEqual(JSONHelper.createJSONObject(expected), json));
    }

    @Test
    public void testResponseCleaning()
            throws Exception {
        final String dirty = "<html><head><title>Document title</title><style></style><script>alert('illegal!')</script></head><body><h4>Allowed</h4></body></html>";
        GetGeoPointDataService partialMock = PowerMockito.spy(new GetGeoPointDataService());
        PowerMockito.doReturn(dirty).when(partialMock, "makeGFIcall", anyString(), anyString(), anyString());
        GFIRequestParams params = new GFIRequestParams();
        final OskariLayer layer = new OskariLayer();
        layer.setUsername("name");
        layer.setPassword("anything");
        layer.setUrl("http://example.com/");
        layer.setName("layerName");
        params.setLayer(layer);
        JSONObject response = partialMock.getWMSFeatureInfo(params);
        assertEquals("Should have cleaned html", response.optString("content"), "<h4>Allowed</h4>");
    }
}
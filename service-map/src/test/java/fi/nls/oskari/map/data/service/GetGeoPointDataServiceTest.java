package fi.nls.oskari.map.data.service;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by SMAKINEN on 24.11.2016.
 */
// @ExtendWith(PowerMockRunner.class)
// @PrepareForTest(GetGeoPointDataService.class)
// these are needed with PowerMock and Java 11. Haven't tried if Java 13+ still needs these:
// https://github.com/powermock/powermock/issues/864
// @PowerMockIgnore({"com.sun.org.apache.xalan.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.w3c.dom.*", "org.xml.*", "com.sun.org.apache.xml.*"})
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
        assertEquals(parsed.optString("Mynd"), "<a target='_blank' href='http://this-is-not-relevant'>link</a>", "Should have html as Mynd");
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
        assertTrue(JSONHelper.isEqual(JSONHelper.createJSONObject(expected), json), "Should match expected");
    }

    /*
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

     */
}
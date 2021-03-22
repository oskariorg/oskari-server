package fi.nls.oskari.util;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oskari.service.wfs3.OskariWFS3Client;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
@PrepareForTest({OskariWFS3Client.class, IOHelper.class, WFSDescribeFeatureHelper.class})
public class WFSGetLayerFieldsTest {
    private final String LAYER_NAME = "layer-name";
    private final String LAYER_URL = "https://example.com/";
    private final String COLLECTION_ITEMS_URL = LAYER_URL + "collections/layer-name/items";
    private final String USERNAME = "username";
    private final String PASSWORD = "pwd";

    @Test
    public void getLayerFieldsForWFS3Collections() throws Exception {
        mockOskariWFS3Client();
        mockIOHelper();
        final OskariLayer layer = getWFSLayer("3.0.0");
        final JSONObject fields = WFSGetLayerFields.getLayerFields(layer);
        assertEquals(fields.getString("geometryName"), "geometry");
        final JSONObject attributes = fields.getJSONObject("types");
        assertEquals(attributes.getString("fid"), "number");
        assertEquals(attributes.getString("attr-1"), "string");
        assertEquals(attributes.getString("attr-2"), "number");
        assertEquals(attributes.getString("attr-3"), "boolean");
        assertEquals(attributes.getString("attr-4"), "unknown");
    }

    @Test
    public void getLayerFieldsForWFSDescribeFeatureType() throws Exception {
        OskariLayer layer = getWFSLayer("2.0.0");
        mockWFSDescribeFeatureHelper(layer);
        final JSONObject fields = WFSGetLayerFields.getLayerFields(layer);
        assertEquals(fields.get("geometryName"), "geom");
        final JSONObject attributes = fields.getJSONObject("types");
        assertEquals(attributes.getString("attr-1"), "number");
        assertEquals(attributes.getString("attr-2"), "string");
        assertEquals(attributes.getString("attr-3"), "number");
        assertEquals(attributes.getString("attr-4"), "string");
        assertEquals(attributes.getString("attr-5"), "unknown");
    }

    private void mockOskariWFS3Client() {
        PowerMockito.mockStatic(OskariWFS3Client.class);
        when(OskariWFS3Client.getItemsPath(eq(LAYER_URL), eq(LAYER_NAME))).thenReturn(COLLECTION_ITEMS_URL);
    }

    private void mockIOHelper() throws IOException {
        PowerMockito.mockStatic(IOHelper.class);
        String rawResponse = ResourceHelper.readStringResource("WFSGetLayerFieldsTest-WFS3CollectionItemsResponse.json", WFSGetLayerFieldsTest.class);
        final HttpURLConnection conn = mock(HttpURLConnection.class);
        when(IOHelper.getConnection(eq(COLLECTION_ITEMS_URL), eq(USERNAME), eq(PASSWORD), any(Map.class), any(Map.class))).thenReturn(conn);
        when(IOHelper.readString(eq(conn.getInputStream()))).thenReturn(rawResponse);
    }

    private void mockWFSDescribeFeatureHelper(OskariLayer layer) throws ServiceException, JSONException {
        PowerMockito.mockStatic(WFSDescribeFeatureHelper.class);
        final JSONObject propertyTypes = new JSONObject();
        propertyTypes.put("attr-1", "xs:int");
        propertyTypes.put("attr-2", "xs:string");
        propertyTypes.put("attr-3", "xs:double");
        propertyTypes.put("attr-4", "xs:date");
        propertyTypes.put("attr-5", "prefix:complex");
        propertyTypes.put("geom", "gml:GeometryPropertyType");
        final JSONObject propertyTypesResponse = new JSONObject();
        propertyTypesResponse.put("propertyTypes", propertyTypes);
        when(WFSDescribeFeatureHelper.getWFSFeaturePropertyTypes(layer, String.valueOf(layer.getId()))).thenReturn(propertyTypesResponse);
    }

    private OskariLayer getWFSLayer(String version) {
        OskariLayer layer = new OskariLayer();
        layer.setType(OskariLayer.TYPE_WFS);
        layer.setId(1);
        layer.setUrl(LAYER_URL);
        layer.setName(LAYER_NAME);
        layer.setUsername(USERNAME);
        layer.setPassword(PASSWORD);
        layer.setVersion(version);
        return layer;
    }
}

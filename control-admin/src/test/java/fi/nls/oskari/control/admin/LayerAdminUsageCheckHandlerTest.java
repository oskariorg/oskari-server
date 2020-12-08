package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oskari.permissions.PermissionService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OskariComponentManager.class})
public class LayerAdminUsageCheckHandlerTest extends JSONActionRouteTest {

    private final static Integer WMS_LAYER_ID = 1;
    private final static Integer SECOND_WMS_LAYER_ID = 2;
    private final static Integer THIRD_WMS_LAYER_ID = 3;
    private final static Integer METADATA_LAYER_ID = 10;

    final private static LayerAdminUsageCheckHandler adminUsageCheckHandler = new LayerAdminUsageCheckHandler();

    final private static LayerAdminHandler adminHandler = new LayerAdminHandler();

    @BeforeClass
    public static void setup() throws Exception {
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getCanonicalName(), true);
        PermissionService permissionService = mock(PermissionService.class);
        OskariLayerService mapLayerService = mock(OskariLayerService.class);
        PowerMockito.mockStatic(OskariComponentManager.class);
        when(OskariComponentManager.getComponentOfType(PermissionService.class)).thenReturn(permissionService);
        when(OskariComponentManager.getComponentOfType(OskariLayerService.class)).thenReturn(mapLayerService);
        doReturn(getTestLayers()).when(mapLayerService).findAll();
        doNothing().when(mapLayerService).update(any());
        doReturn(new HashSet<String>()).when(permissionService).getAdditionalPermissions();
        doAnswer(invocation -> invocation.getArgument(0)).when(permissionService).getPermissionName(anyString(),anyString());
        adminUsageCheckHandler.init();
        adminHandler.init();
    }

    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }

    private static List<OskariLayer> getTestLayers() throws JSONException {
        List<OskariLayer> layers = new ArrayList<>();
        layers.add(createLayer(METADATA_LAYER_ID, "areas:100k", OskariLayer.TYPE_WFS, "https://test/wfs", null));
        layers.add(createLayer(WMS_LAYER_ID, "areas:10k", OskariLayer.TYPE_WMS, "https://test/wms", METADATA_LAYER_ID));
        layers.add(createLayer(SECOND_WMS_LAYER_ID, "areas:100k", OskariLayer.TYPE_WMS, "https://test/wms", null));
        layers.add(createLayer(THIRD_WMS_LAYER_ID, "areas:1000k", OskariLayer.TYPE_WMS, "https://test/wms", METADATA_LAYER_ID));
        return layers;
    }

    private static OskariLayer createLayer(Integer wmsLayerId, String name, String layerType, String url, Integer metadataLayerId) throws JSONException {
        OskariLayer wmsLayer = new OskariLayer();
        wmsLayer.setId(wmsLayerId);
        wmsLayer.setType(layerType);
        wmsLayer.setUrl(url);
        wmsLayer.setName(name);
        if (metadataLayerId != null) {
            createTimeseriesMetadata(wmsLayer, metadataLayerId);
        }
        return wmsLayer;
    }

    private static void createTimeseriesMetadata(OskariLayer wmsLayer, Integer metadataLayerId) throws JSONException {
        JSONObject metadata = new JSONObject().put("layer", metadataLayerId);
        JSONObject timeseries = new JSONObject().put("metadata", metadata);
        wmsLayer.setOptions(new JSONObject().put("timeseries", timeseries));
    }

    @Test
    public void testLayerUsageCheck() throws Exception {
        Map requestParameters = getRequestParams();
        ActionParameters params = createActionParams(requestParameters, getAdminUser());
        adminUsageCheckHandler.handleAction(params);
        JSONObject responseJson = ResourceHelper.readJSONResource("LayerAdminUsageCheckHandlerTests-expected.json", this);
        verifyResponseContent(responseJson);
    }

    @Test
    public void testLayerUsageCheckWhenUserIsNotAdmin() {
        Map requestParameters = getRequestParams();
        ActionParameters params = createActionParams(requestParameters, getNotAdminUser());
        try {
            adminUsageCheckHandler.handleAction(params);
            fail("ActionDeniedException should have been thrown");
        } catch (ActionException e) {
            assertEquals("Admin only", e.getMessage());
        }
    }

    @Test
    public void testCleanupLayerReferences() throws Exception {
        List<OskariLayer> layers = adminHandler.cleanupLayerReferences(METADATA_LAYER_ID);
        for (OskariLayer layer : layers) {
            assertNull(layer.getOptions().optJSONObject("timeseries"));
        }
    }

    private Map getRequestParams() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("id", METADATA_LAYER_ID.toString());
        return parameters;
    }
}

package fi.nls.oskari.control.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.permissions.PermissionService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractLayerAdminHandlerTest extends JSONActionRouteTest {

    protected final static Integer WMS_LAYER_ID = 1;
    protected final static Integer SECOND_WMS_LAYER_ID = 2;
    protected final static Integer THIRD_WMS_LAYER_ID = 3;
    protected final static Integer METADATA_LAYER_ID = 10;

    protected static void setupMocks() throws Exception {
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getCanonicalName(), true);
        PermissionService permissionService = mock(PermissionService.class);
        OskariLayerService mapLayerService = mock(OskariLayerService.class);
        // org.mockito.exceptions.base.MockitoException:
        // For fi.nls.oskari.service.OskariComponentManager, static mocking is already registered in the current thread
//        Mockito.mockStatic(OskariComponentManager.class);
        when(OskariComponentManager.getComponentOfType(PermissionService.class)).thenReturn(permissionService);
        when(OskariComponentManager.getComponentOfType(OskariLayerService.class)).thenReturn(mapLayerService);
        doReturn(getTestLayers()).when(mapLayerService).findAll();
        doNothing().when(mapLayerService).update(any());
        doReturn(new HashSet<String>()).when(permissionService).getAdditionalPermissions();
        // unneccessary-stubbing-exception
//        doAnswer(invocation -> invocation.getArgument(0)).when(permissionService).getPermissionName(anyString(),anyString());

    }

    protected static void tearDownMocks() {
        PropertyUtil.clearProperties();
    }

    protected static List<OskariLayer> getTestLayers() throws JSONException {
        List<OskariLayer> layers = new ArrayList<>();
        layers.add(createLayer(METADATA_LAYER_ID, "areas:100k", OskariLayer.TYPE_WFS, "https://test/wfs", null));
        layers.add(createLayer(WMS_LAYER_ID, "areas:10k", OskariLayer.TYPE_WMS, "https://test/wms", METADATA_LAYER_ID));
        layers.add(createLayer(SECOND_WMS_LAYER_ID, "areas:100k", OskariLayer.TYPE_WMS, "https://test/wms", null));
        layers.add(createLayer(THIRD_WMS_LAYER_ID, "areas:1000k", OskariLayer.TYPE_WMS, "https://test/wms", METADATA_LAYER_ID));
        return layers;
    }

    protected static OskariLayer createLayer(Integer wmsLayerId, String name, String layerType, String url, Integer metadataLayerId) throws JSONException {
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

    protected static void createTimeseriesMetadata(OskariLayer wmsLayer, Integer metadataLayerId) throws JSONException {
        JSONObject metadata = new JSONObject().put("layer", metadataLayerId);
        JSONObject timeseries = new JSONObject().put("metadata", metadata);
        wmsLayer.setOptions(new JSONObject().put("timeseries", timeseries));
    }
}

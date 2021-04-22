package fi.nls.oskari.control.layer;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.WFSGetLayerFields;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GetWFSLayerFieldsHandler.class, WFSGetLayerFields.class})
public class GetWFSLayerFieldsHandlerTest extends JSONActionRouteTest {
    private static final Integer WFS_LAYER_ID = 1;
    private static final Integer WMS_LAYER_ID = 2;
    private static final String PARAM_LAYER_ID = "layer_id";
    private static final GetWFSLayerFieldsHandler handler = new GetWFSLayerFieldsHandler();

    @BeforeClass
    public static void setup() throws Exception {
        mockWFSGetLayerFields();
        mockPermissionHelper();
        handler.init();
    }

    private static void mockWFSGetLayerFields() throws Exception {
        PowerMockito.mockStatic(WFSGetLayerFields.class);
        final JSONObject mockFields = getMockFields();
        when(WFSGetLayerFields.getLayerFields(any())).thenReturn(mockFields);
    }

    private static JSONObject getMockFields() throws JSONException {
        final String rawFieldsStr = "{\"types\": {\"test-attribute\": \"string\"}, \"geometryName\": \"geometry\"}";
        return new JSONObject(rawFieldsStr);
    }

    private static void mockPermissionHelper() throws Exception {
        PermissionHelper mockPermissionHelper = mock(PermissionHelper.class);
        when(mockPermissionHelper.getLayer(anyInt(), any(User.class))).thenAnswer(invocation -> {
            final Integer layerId = invocation.getArgument(0);
            if (layerId.equals(WFS_LAYER_ID)) {
                return getMockLayer(OskariLayer.TYPE_WFS);
            } else if (layerId.equals(WMS_LAYER_ID)) {
                return getMockLayer(OskariLayer.TYPE_WMS);
            } else {
                throw new ActionParamsException("Layer not found for id: " + layerId);
            }
        });
        handler.setPermissionHelper(mockPermissionHelper);
    }

    private static OskariLayer getMockLayer(String layerType) throws JSONException {
        OskariLayer wfsLayer = new OskariLayer();
        wfsLayer.setType(layerType);
        final String rawJsonStr = "{\"data\": {\"locale\": {\"fi\": {\"test-attribute\": \"label for fi\"}},\"filter\":[\"test-attribute\"]}}";
        final JSONObject mockLayerAttributes = new JSONObject(rawJsonStr);
        wfsLayer.setAttributes(mockLayerAttributes);
        return wfsLayer;
    }

    @Test
    // We already have test for most of the logic. This test can be just for checking permissions
    @Ignore("Mocking doesn't work properly with some timing issue that crashes build sometimes")
    public void handleActionShouldReturnCorrectResponse() throws Exception {
        final ActionParameters params = getActionParameters("1");
        handler.handleAction(params);
        JSONObject expectedResult = ResourceHelper.readJSONResource("GetWFSLayerFieldsHandlerTest-expected.json", this);
        verifyResponseContent(expectedResult);
    }

    @Test(expected =  ActionException.class)
    public void handleActionShouldThrowActionExceptionForNonWFSLayer() throws  Exception {
        final ActionParameters params = getActionParameters("2");
        handler.handleAction(params);
    }

    @Test(expected = ActionException.class)
    public void handleActionShouldThrowActionExceptionForInvalidLayerId() throws Exception {
        final ActionParameters params = getActionParameters("3");
        handler.handleAction(params);
    }

    private ActionParameters getActionParameters(String layerId) {
        final Map<String, String> params = new HashMap<>();
        params.put(PARAM_LAYER_ID, layerId);
        final ActionParameters actionParams = createActionParams(params);
        final User mockUser = mock(User.class);
        actionParams.setUser(mockUser);
        return actionParams;
    }
}

package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


@ExtendWith(MockitoExtension.class)
public class LayerAdminUsageCheckHandlerTest extends AbstractLayerAdminHandlerTest {

    final private static LayerAdminUsageCheckHandler handler = new LayerAdminUsageCheckHandler();

    @BeforeEach
    public void setup() throws Exception {
        setupMocks();
        handler.init();
    }

    @AfterEach
    public void tearDown() {
        OskariComponentManager.teardown();
        tearDownMocks();
    }

    @Test
    public void testLayerUsageCheck() throws Exception {
        Map requestParameters = getRequestParams();
        ActionParameters params = createActionParams(requestParameters, getAdminUser());
        handler.handleAction(params);
        JSONObject responseJson = ResourceHelper.readJSONResource("LayerAdminUsageCheckHandlerTests-expected.json", this);
        verifyResponseContent(responseJson);
    }

    @Test
    public void testLayerUsageCheckWhenUserIsNotAdmin() {
        Map requestParameters = getRequestParams();
        ActionParameters params = createActionParams(requestParameters, getNotAdminUser());
        try {
            handler.handleAction(params);
            fail("ActionDeniedException should have been thrown");
        } catch (ActionException e) {
            assertEquals("Admin only", e.getMessage());
        }
    }

    private Map getRequestParams() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("id", METADATA_LAYER_ID.toString());
        return parameters;
    }
}

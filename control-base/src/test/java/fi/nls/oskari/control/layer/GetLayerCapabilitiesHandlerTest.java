package fi.nls.oskari.control.layer;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.TestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;
import org.oskari.permissions.model.*;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
public class GetLayerCapabilitiesHandlerTest extends JSONActionRouteTest {

    private static final String TEST_DATA = "test data";
    private GetLayerCapabilitiesHandler handler = null;

    @Before
    public void setup() {
        assumeTrue(TestHelper.dbAvailable());
        OskariLayerService layerService = getOskariLayerService();
        PermissionService permissionsService = getPermissionsService();
        handler = new GetLayerCapabilitiesHandler();

        PermissionHelper helper = new PermissionHelper(layerService, permissionsService);
        handler.setPermissionHelper(helper);
        handler.init();
    }
    @Test(expected = ActionDeniedException.class)
    public void testHandleActionGuest()
            throws Exception {
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put(ActionConstants.KEY_ID, "1");
        ActionParameters params = createActionParams(httpParams);
        // only gave logged in user role permission so this should throw ActionDeniedException
        handler.handleAction(params);

        fail("Should have thrown exception");
    }

    @Test
    public void testHandleActionUser()
            throws Exception {
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put(ActionConstants.KEY_ID, "1");
        ActionParameters params = createActionParams(httpParams, getLoggedInUser());
        handler.handleAction(params);
        // logged in user has permission so this should TEST_DATA from mock service
        verifyResponseContent(TEST_DATA);
    }

    @Test(expected = ActionException.class)
    public void testHandleServiceException()
            throws Exception {
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put(ActionConstants.KEY_ID, "2");
        ActionParameters params = createActionParams(httpParams, getLoggedInUser());
        handler.handleAction(params);

        fail("Should have thrown exception");
    }

    /* *********************************************
     * Service mocks
     * ********************************************
     */
    private OskariLayerService getOskariLayerService() {

        OskariLayerService layerService = mock(OskariLayerServiceMybatisImpl.class);

        OskariLayer layer = new OskariLayer();
        layer.setType("WMTS");
        doReturn(layer).when(layerService).find(1);
        OskariLayer errorLayer = new OskariLayer();
        errorLayer.setType("ERROR");
        doReturn(errorLayer).when(layerService).find(2);
        return layerService;
    }
    private PermissionService getPermissionsService() {

        PermissionService service = mock(PermissionServiceMybatisImpl.class);

        Resource res = new Resource();
        Permission p = new Permission();
        p.setType(PermissionType.VIEW_LAYER);
        p.setExternalType(PermissionExternalType.ROLE);
        p.setExternalId("" + getLoggedInUser().getRoles().iterator().next().getId());
        res.addPermission(p);
        doReturn(res).when(service).findResource(ResourceType.maplayer, any(String.class));
        return service;
    }

    @AfterClass
    public static void delete() {
        PropertyUtil.clearProperties();
    }

}
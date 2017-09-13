package fi.nls.oskari.control.layer;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.permission.domain.Permission;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.TestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by SMAKINEN on 28.8.2015.
 */
@RunWith(PowerMockRunner.class)
public class GetLayerCapabilitiesHandlerTest extends JSONActionRouteTest {

    private static final String TEST_DATA = "test data";
    private GetLayerCapabilitiesHandler handler = null;

    @Before
    public void setup() {
        assumeTrue(TestHelper.dbAvailable());
        OskariLayerService layerService = getOskariLayerService();
        PermissionsService permissionsService = getPermissionsService();
        // replace the cache service with a test service
        OskariComponentManager.removeComponentsOfType(CapabilitiesCacheService.class);
        OskariComponentManager.addComponent(new CapabilitiesCacheServiceMock(TEST_DATA));
        //CapabilitiesCacheService service = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);
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

        OskariLayerService layerService = mock(OskariLayerServiceIbatisImpl.class);

        OskariLayer layer = new OskariLayer();
        layer.setType("WMTS");
        doReturn(layer).when(layerService).find("1");
        OskariLayer errorLayer = new OskariLayer();
        errorLayer.setType("ERROR");
        doReturn(errorLayer).when(layerService).find("2");
        return layerService;
    }
    private PermissionsService getPermissionsService() {

        PermissionsService service = mock(PermissionsServiceIbatisImpl.class);

        Resource res = new Resource();
        Permission p = new Permission();
        p.setType(Permissions.PERMISSION_TYPE_VIEW_LAYER);
        p.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
        p.setExternalId("" + getLoggedInUser().getRoles().iterator().next().getId());
        res.addPermission(p);
        doReturn(res).when(service).findResource(any(Resource.class));
        return service;
    }

    @AfterClass
    public static void delete() {
        PropertyUtil.clearProperties();
    }

}
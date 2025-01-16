package fi.nls.oskari.control.view;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.view.modifier.param.CoordinateParamHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.DataProviderServiceMybatisImpl;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ViewModifier;
import fi.nls.oskari.wfs.WFSSearchChannelsService;
import fi.nls.oskari.wfs.WFSSearchChannelsServiceMybatisImpl;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.TestHelper;
import fi.nls.test.view.BundleTestHelper;
import fi.nls.test.view.ViewTestHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.ResourceType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 31.5.2013
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
@ExtendWith(MockitoExtension.class)
@Disabled
// TODO: fix
// org.mockito.exceptions.base.MockitoException:
// For fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl, static mocking is already registered in the current thread
public class GetAppSetupHandlerTest extends JSONActionRouteTest {

    final private GetAppSetupHandler handler = new GetAppSetupHandler();

    private ViewService viewService = null;
    private BundleService bundleService = null;

    @BeforeAll
    public static void addLocales() throws Exception {
        TestHelper.registerTestDataSource();
        Properties properties = new Properties();
        try {
            properties.load(GetAppSetupHandlerTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
            PropertyUtil.addProperty("oskari.user.service", "fi.nls.oskari.service.DummyUserService", true);
            String locales = PropertyUtil.getNecessary("oskari.locales");
            if (locales == null)
                fail("No darned locales");
        } catch (DuplicateException e) {
            fail("Should not throw exception" + e.getStackTrace());
        }
        // To get fresh start for components
        OskariComponentManager.teardown();
    }
    @BeforeEach
    public void setUp() throws Exception {
        mockViewService();
        mockBundleService();
        mockInternalServices();

        handler.setViewService(viewService);
        handler.setBundleService(bundleService);

        handler.init();
    }
    @AfterAll
    public static void teardown() {
        PropertyUtil.clearProperties();
        // To get fresh start for components
        OskariComponentManager.teardown();
        TestHelper.teardown();
    }

    @Test
    public void testURLsWithRegistrationAllowed() throws Exception {
        PropertyUtil.addProperty("allow.registration", "true");
        final ActionParameters params = createActionParams();
        handler.handleAction(params);

        JSONObject responseUrls = getResponseJSON().optJSONObject("env").optJSONObject("urls");
        assertEquals("/user", responseUrls.opt("register"), "Should have default register url");
        assertEquals("/user", responseUrls.opt("profile"), "Should have default profile url");

        teardown();
        addLocales();
    }

    @Test
    public void testLoginUrl() throws Exception {
        PropertyUtil.addProperty("auth.login.url", "/login");
        final ActionParameters params = createActionParams();
        handler.handleAction(params);

        JSONObject responseUrls = getResponseJSON().optJSONObject("env").optJSONObject("urls");
        assertEquals("/login", responseUrls.opt("login"), "Should have login url");
        assertNull(responseUrls.opt("register"), "Should NOT have register url as allow.registration not set");
        teardown();
        addLocales();
    }


    @Test
    public void testWithNoViewIdAndGuestUser() throws Exception {
        final ActionParameters params = createActionParams();
        handler.handleAction(params);

        // check that view was loaded with id 2 as we mocked the default view to be for guest user
        verify(viewService, times(1)).getViewWithConf(2);

        // check that the guest view matches
        GetAppSetupTestHelper.verifyResponseContent("GetAppSetupHandlerTest-view-guest.json", getResponseJSON());
    }

    @Test
    public void testWithNoViewIdAndLoggedInUser() throws Exception {
        final ActionParameters params = createActionParams(getLoggedInUser());
        handler.handleAction(params);

        // check that view was loaded with id 1 as we mocked the default view to be logged in user
        verify(viewService, times(1)).getViewWithConf(1);

        // check that the user is written to the config
        GetAppSetupTestHelper.verifyResponseContent("GetAppSetupHandlerTest-view-loggedin.json", getResponseJSON());
    }

    @Test
    public void testWithViewIdGiven() throws Exception {
        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ActionConstants.PARAM_VIEW_ID, "3");
        // TODO: setup a cookie with state and see that it shouldn't change the view since a specific non-default view was requested
        // TODO: create a test without giving viewId and see that the cookie affects it
        final ActionParameters params = createActionParams(parameters);
        handler.handleAction(params);

        // check that view was loaded with id 3 as requested
        verify(viewService, times(1)).getViewWithConf(3);

        // check that the response matches expected
        GetAppSetupTestHelper.verifyResponseContent("GetAppSetupHandlerTest-view-3.json", getResponseJSON());
    }



    @Test
    public void testWithCoordinateParameterGiven() throws Exception {
        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        CoordinateParamHandler h = new CoordinateParamHandler();
        parameters.put(h.getName(), "123_456");

        final ActionParameters params = createActionParams(parameters);
        handler.handleAction(params);
        JSONObject response = getResponseJSON();
        JSONObject responseState = response.optJSONObject("configuration").optJSONObject("mapfull").optJSONObject("state");
        assertEquals("123", responseState.opt("east"), "Should have requested east");
        assertEquals("456", responseState.opt("north"), "Should have requested north");
        // coordinates should be set as in param and Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin plugin should have been removed from config
        GetAppSetupTestHelper.verifyResponseContent("GetAppSetupHandlerTest-coordinate-params.json", response);
    }

    @Test
    public void testWithOldIdGiven() throws Exception {
        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ActionConstants.PARAM_VIEW_ID, "456");
        parameters.put(GetAppSetupHandler.PARAM_OLD_ID, "123");
        // TODO: setup a cookie with state and see that it shouldn't change the view since a migrated view was requested
        final ActionParameters params = createActionParams(parameters);
        handler.handleAction(params);

        // check that view was not loaded with id, but with old Id
        verify(viewService, never()).getViewWithConf(anyLong());
        verify(viewService, times(1)).getViewWithConfByOldId(123);
    }
    /* *********************************************
     * Service mocks
     * ********************************************
     */
    private void mockViewService() {

        viewService = mock(AppSetupServiceMybatisImpl.class);
        // id 2 for guest user
        doReturn(2L).when(viewService).getDefaultViewId(getGuestUser());
        // id 1 for logged in user
        doReturn(1L).when(viewService).getDefaultViewId(getLoggedInUser());

        // id 1 for default
        doReturn(1L).when(viewService).getDefaultViewId();

        final View dummyView = ViewTestHelper.createMockView("framework.mapfull");
        dummyView.setType(ViewTypes.USER);
        dummyView.setOnlyForUuId(false);
        doReturn(dummyView).when(viewService).getViewWithConfByOldId(anyLong());
        doReturn(dummyView).when(viewService).getViewWithConf(anyLong());
        doReturn(dummyView).when(viewService).getViewWithConfByUuId(anyString());

        // TODO: mock view loading
        /**
         * fi.nls.oskari.control.ActionException: Could not get View with id: 2 and oldId: -1
         *      at fi.nls.oskari.control.view.GetAppSetupHandler.handleAction(GetAppSetupHandler.java:136)
         */
    }



    private void mockBundleService() throws Exception {

        bundleService = mock(BundleServiceMybatisImpl.class);
        doReturn(
                BundleTestHelper.loadBundle("integration.admin-layerselector")
        ).when(bundleService).getBundleTemplateByName(ViewModifier.BUNDLE_ADMINLAYERSELECTOR);

        doReturn(
                BundleTestHelper.loadBundle("framework.postprocessor")
        ).when(bundleService).getBundleTemplateByName(ViewModifier.BUNDLE_POSTPROCESSOR);

        Mockito.mockConstructionWithAnswer(BundleServiceMybatisImpl.class, invocation -> {
            if (invocation.getArguments().length == 0) {
                return bundleService;
            }

            return invocation.callRealMethod();
        });
    }

    private void mockInternalServices() throws Exception {

        final PermissionService service = mock(PermissionServiceMybatisImpl.class);
        // permission check is skipped here so just mock the call
        doReturn(Optional.empty()).when(service).findResource(eq(ResourceType.maplayer.name()), any(String.class));
        doReturn(Collections.emptySet()).when(service).getResourcesWithGrantedPermissions(eq(AnalysisLayer.TYPE), any(User.class), eq(PermissionType.VIEW_PUBLISHED.name()));

        Mockito.mockConstructionWithAnswer(PermissionServiceMybatisImpl.class, invocation -> {
            if (invocation.getArguments().length == 0) {
                return service;
            }

            return invocation.callRealMethod();
        });

        final WFSSearchChannelsService searchService = mock(WFSSearchChannelsServiceMybatisImpl.class);
        doReturn(Collections.emptyList()).when(searchService).findChannels();

        Mockito.mockConstructionWithAnswer(WFSSearchChannelsServiceMybatisImpl.class, invocation -> {
            if (invocation.getArguments().length == 0) {
                return searchService;
            }

            return invocation.callRealMethod();
        });

        // TODO: this might not do nearly the same thing as suppress(constructor()). Might need to come back to this.
        // All in all, we gotta move these mockings to somewhere like beforeall and beforeeach and such. Skipping for now.
        Mockito.mockConstruction(OskariLayerServiceMybatisImpl.class);
//        suppress(constructor(OskariLayerServiceMybatisImpl.class));

        final OskariLayerServiceMybatisImpl layerService = mock(OskariLayerServiceMybatisImpl.class);
        doReturn(null).when(layerService).find(anyInt());
        doReturn(Collections.emptyList()).when(layerService).findAll();

        Mockito.mockConstructionWithAnswer(OskariLayerServiceMybatisImpl.class, invocation -> {
            if (invocation.getArguments().length == 0) {
                return layerService;
            }

            return invocation.getMock();
        });


        // TODO: Gotta figure out the junit5 equivalent of doing this
        //Whitebox.newInstance(DataProviderServiceIbatisImpl.class);
        final DataProviderService groupService = mock(DataProviderServiceMybatisImpl.class);
        DataProvider group = mock(DataProvider.class);
        group.setName("en", "Testing");
        doReturn(group).when(groupService).find(anyInt());
        doReturn(Collections.emptyList()).when(groupService).findAll();

        Mockito.mockConstructionWithAnswer(DataProviderServiceMybatisImpl.class, invocation -> {
            if (invocation.getArguments().length == 0) {
                return groupService;
            }

            return invocation.getMock();
        });
    }

}

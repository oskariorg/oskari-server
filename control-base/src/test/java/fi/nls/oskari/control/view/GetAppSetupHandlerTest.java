package fi.nls.oskari.control.view;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.view.modifier.bundle.MapfullHandler;
import fi.nls.oskari.control.view.modifier.param.CoordinateParamHandler;
import fi.nls.oskari.control.view.modifier.param.WFSHighlightParamHandler;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.DataProviderServiceMybatisImpl;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ViewModifier;
import fi.nls.oskari.wfs.WFSSearchChannelsService;
import fi.nls.oskari.wfs.WFSSearchChannelsServiceMybatisImpl;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import fi.nls.test.view.BundleTestHelper;
import fi.nls.test.view.ViewTestHelper;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.ResourceType;
import org.oskari.service.util.ServiceFactory;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 31.5.2013
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {WFSHighlightParamHandler.class, OskariLayerWorker.class, PropertyUtil.class, MapfullHandler.class, ServiceFactory.class})
// these are needed with PowerMock and Java 11. Haven't tried if Java 13+ still needs these:
// https://github.com/powermock/powermock/issues/864
@PowerMockIgnore({"com.sun.org.apache.xalan.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.w3c.dom.*", "org.xml.*", "com.sun.org.apache.xml.*"})
public class GetAppSetupHandlerTest extends JSONActionRouteTest {

    final private GetAppSetupHandler handler = new GetAppSetupHandler();

    private ViewService viewService = null;
    private BundleService bundleService = null;

    @BeforeClass
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
    @Before
    public void setUp() throws Exception {
        mockViewService();
        mockBundleService();
        mockInternalServices();

        handler.setViewService(viewService);
        handler.setBundleService(bundleService);

        handler.init();
    }
    @AfterClass
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
        assertEquals("Should have default register url", "/user", responseUrls.opt("register"));
        assertEquals("Should have default profile url", "/user", responseUrls.opt("profile"));

        teardown();
        addLocales();
    }

    @Test
    public void testLoginUrl() throws Exception {
        PropertyUtil.addProperty("auth.login.url", "/login");
        final ActionParameters params = createActionParams();
        handler.handleAction(params);

        JSONObject responseUrls = getResponseJSON().optJSONObject("env").optJSONObject("urls");
        assertEquals("Should have login url", "/login", responseUrls.opt("login"));
        assertNull("Should NOT have register url as allow.registration not set", responseUrls.opt("register"));
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
        verifyResponseContent(ResourceHelper.readJSONResource("GetAppSetupHandlerTest-view-guest.json", this));
    }

    @Test
    public void testWithNoViewIdAndLoggedInUser() throws Exception {
        final ActionParameters params = createActionParams(getLoggedInUser());
        handler.handleAction(params);

        // check that view was loaded with id 1 as we mocked the default view to be logged in user
        verify(viewService, times(1)).getViewWithConf(1);

        // check that the user is written to the config
        verifyResponseContent(ResourceHelper.readJSONResource("GetAppSetupHandlerTest-view-loggedin.json", this));
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
        verifyResponseContent(ResourceHelper.readJSONResource("GetAppSetupHandlerTest-view-3.json", this));
    }



    @Test
    public void testWithCoordinateParameterGiven() throws Exception {
        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        CoordinateParamHandler h = new CoordinateParamHandler();
        parameters.put(h.getName(), "123_456");

        final ActionParameters params = createActionParams(parameters);
        handler.handleAction(params);

        JSONObject responseState = getResponseJSON().optJSONObject("configuration").optJSONObject("mapfull").optJSONObject("state");
        assertEquals("Should have requested east", "123", responseState.opt("east"));
        assertEquals("Should have requested north", "456", responseState.opt("north"));
        // coordinates should be set as in param and Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin plugin should have been removed from config
        verifyResponseContent(ResourceHelper.readJSONResource("GetAppSetupHandlerTest-coordinate-params.json", this));
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

        // return mocked  bundle service if a new one is created (in paramhandlers for example)
        // classes doing this must be listed in PrepareForTest annotation
        whenNew(BundleServiceMybatisImpl.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return bundleService;
                    }
                });
    }

    private void mockInternalServices() throws Exception {

        final PermissionService service = mock(PermissionServiceMybatisImpl.class);
        // permission check is skipped here so just mock the call
        doReturn(Optional.empty()).when(service).findResource(eq(ResourceType.maplayer.name()), any(String.class));
        doReturn(Collections.emptySet()).when(service).getResourcesWithGrantedPermissions(eq(AnalysisLayer.TYPE), any(User.class), eq(PermissionType.VIEW_PUBLISHED.name()));


        // return mocked  bundle service if a new one is created (in paramhandlers for example)
        // classes doing this must be listed in PrepareForTest annotation
        whenNew(PermissionServiceMybatisImpl.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) {
                        return service;
                    }
                });


        final WFSSearchChannelsService searchService = mock(WFSSearchChannelsServiceMybatisImpl.class);
        doReturn(Collections.emptyList()).when(searchService).findChannels();
        whenNew(WFSSearchChannelsServiceMybatisImpl.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) {
                        return searchService;
                    }
                });

        suppress(constructor(OskariLayerServiceMybatisImpl.class));
        final OskariLayerServiceMybatisImpl layerService = mock(OskariLayerServiceMybatisImpl.class);
        doReturn(null).when(layerService).find(anyInt());
        doReturn(Collections.emptyList()).when(layerService).findAll();

        // return mocked  bundle service if a new one is created (in paramhandlers for example)
        // classes doing this must be listed in PrepareForTest annotation
        whenNew(OskariLayerServiceMybatisImpl.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return layerService;
                    }
                });

        //Whitebox.newInstance(DataProviderServiceIbatisImpl.class);
        final DataProviderService groupService = mock(DataProviderServiceMybatisImpl.class);
        DataProvider group = mock(DataProvider.class);
        group.setName("en", "Testing");
        doReturn(group).when(groupService).find(anyInt());
        doReturn(Collections.emptyList()).when(groupService).findAll();

        // return mocked  bundle service if a new one is created (in paramhandlers for example)
        // classes doing this must be listed in PrepareForTest annotation
        whenNew(DataProviderServiceMybatisImpl.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return groupService;
                    }
                });
    }

}

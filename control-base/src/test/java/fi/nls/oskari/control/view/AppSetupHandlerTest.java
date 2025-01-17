package fi.nls.oskari.control.view;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.MyPlacesServiceMybatisImpl;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ViewModifier;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import fi.nls.test.view.ViewTestHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class AppSetupHandlerTest extends JSONActionRouteTest {
	
    private AppSetupHandler handler = null;
    private ViewService viewService = null;
    private MyPlacesService myPlaceService = null;
    private PermissionService permissionsService = null;
    private UserService userService = null;
    private BundleService bundleService = null;

    public static final String BUNDLE_WHITELISTED = "whitelistTestBundle";
    public static final String VALUE_PARENT_UUID = "just-testing";

    @BeforeAll
    public static void addProperties() throws Exception {
        TestHelper.registerTestDataSource();
        PropertyUtil.addProperty("view.template.publish", "3", true);
        PropertyUtil.addProperty("oskari.domain", "//domain.com", true);
        PropertyUtil.addProperty("oskari.map.url", "/map", true);
    }

    @BeforeEach
    public void setUp() throws Exception {
        // view.template.publish=3
    	// mock services for testing
        handler = new AppSetupHandler();
    	mockViewService();
        myPlaceService = mock(MyPlacesServiceMybatisImpl.class);
        permissionsService = mock(PermissionServiceMybatisImpl.class);
        mockBundleService();
        mockUserService();

        // set mocked services
        handler.setViewService(viewService);
        handler.setMyPlacesService(myPlaceService);
        handler.setUserLayerService(mock(UserLayerDbService.class));
        handler.setAnalysisService(mock(AnalysisDbService.class));
        handler.setPermissionsService(permissionsService);
        handler.setBundleService(bundleService);

        handler.init();
    }

    @AfterAll
    public static void teardown() {
        PropertyUtil.clearProperties();
        TestHelper.teardown();
    }

    private void mockViewService() {
        viewService = mock(AppSetupServiceMybatisImpl.class);
        // add all bundles needed in test
        final View dummyView = ViewTestHelper.createMockView("framework.mapfull", "framework.infobox", "framework.publishedgrid");
        dummyView.setType(ViewTypes.USER);
        dummyView.setCreator(getLoggedInUser().getId());
        // inject mapOptions as it's required by publisher
        JSONHelper.putValue(dummyView.getBundleByName(ViewModifier.BUNDLE_MAPFULL).getConfigJSON(), "mapOptions", JSONHelper.createJSONObject("srsName", "EPSG:3067"));
        doReturn(dummyView).when(viewService).getViewWithConf(anyLong());
        doReturn(dummyView).when(viewService).getViewWithConfByUuId(VALUE_PARENT_UUID);
    }
    private void mockBundleService() {
        bundleService = mock(BundleServiceMybatisImpl.class);
        // add all bundles needed in test
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_WHITELISTED);
        Mockito.lenient().doReturn(bundle).when(bundleService).getBundleTemplateByName(BUNDLE_WHITELISTED);
    }

    private void mockUserService() throws Exception {
        userService = mock(UserService.class);
        Role role = new Role();
        role.setName("Admin");
        Mockito.lenient().doReturn(role).when(userService).getRoleByName(role.getName());
        Field instance = UserService.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, userService);
        // Whitebox.setInternalState(UserService.class, "instance", userService);
    }

    @Test
    public void testPublishFromTemplateSimpleInput() throws Exception {

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(AppSetupHandler.KEY_PUBDATA, ResourceHelper.readStringResource("AppSetupHandlerTest-input-simple.json", this));
        parameters.put(AppSetupHandler.PARAM_PUBLISHER_VIEW_UUID, VALUE_PARENT_UUID);

        final ActionParameters params = createActionParams(parameters, getLoggedInUser());

        verifyResponseNotWritten(params);
        handler.handlePost(params);
        // test that response was written once
        verifyResponseWritten(params);
        final JSONObject expectedResult = ResourceHelper.readJSONResource("AppSetupHandlerTest-output-simple.json", this);
        final JSONObject actualResponse = getResponseJSON();
        // UUID will change in each run, so just checking that there is one
        assertNotNull(actualResponse.getString("uuid"), "Must contain actual UUID");
        actualResponse.remove("uuid");
        expectedResult.remove("uuid");

        // URL will change in each run as it contains the UUID, so just checking that there is one
        assertNotNull( actualResponse.getString("url"), "Must contain some URL");
        assertNotNull(actualResponse.getString("url").startsWith("//domain.com/map?lang=fi&" + ActionConstants.PARAM_UUID + "="), "URL should start with expected format");
                actualResponse.remove("url");
        expectedResult.remove("url");

        assertTrue(JSONHelper.isEqual(expectedResult, actualResponse), "Response should match expected");
    }

    @Test
    public void testWhiteListConfigMismatch() throws Exception {

        PropertyUtil.addProperty("actionhandler.AppSetup.bundles.simple", "", true);
        handler.init();
        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(AppSetupHandler.KEY_PUBDATA, ResourceHelper.readStringResource("AppSetupHandlerTest-input-whitelist.json", this));
        parameters.put(AppSetupHandler.PARAM_PUBLISHER_VIEW_UUID, VALUE_PARENT_UUID);

        final ActionParameters params = createActionParams(parameters, getLoggedInUser());
        View view = handler.buildPublishedView(params);

        assertNull(view.getBundleByName(BUNDLE_WHITELISTED), "View shouldn't have bundle that has not been whitelisted");
    }

    @Test
    public void testWhiteListConfigMatch() throws Exception {

        PropertyUtil.addProperty("actionhandler.AppSetup.bundles.simple", BUNDLE_WHITELISTED, true);
        handler.init();
        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(AppSetupHandler.KEY_PUBDATA, ResourceHelper.readStringResource("AppSetupHandlerTest-input-whitelist.json", this));
        parameters.put(AppSetupHandler.PARAM_PUBLISHER_VIEW_UUID, VALUE_PARENT_UUID);

        final ActionParameters params = createActionParams(parameters, getLoggedInUser());
        View view = handler.buildPublishedView(params);

        assertNotNull(view.getBundleByName(BUNDLE_WHITELISTED), "View should have bundle that has been whitelisted");
    }

}


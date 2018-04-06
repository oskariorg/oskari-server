package fi.nls.oskari.control.view;

import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.MyPlacesServiceMybatisImpl;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ViewModifier;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.view.ViewTestHelper;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {UserService.class})
public class AppSetupHandlerTest extends JSONActionRouteTest {
	
    private AppSetupHandler handler = null;
    private ViewService viewService = null;
    private MyPlacesService myPlaceService = null;
    private PermissionsService permissionsService = null;
    private UserService userService = null;
    private BundleService bundleService = null;

    public static final String BUNDLE_WHITELISTED = "whitelistTestBundle";
    public static final String VALUE_PARENT_UUID = "just-testing";

    @BeforeClass
    public static void addProperties() throws Exception {
        PropertyUtil.addProperty("view.template.publish", "3", true);
        PropertyUtil.addProperty("oskari.domain", "//domain.com", true);
        PropertyUtil.addProperty("oskari.map.url", "/map", true);
    }

    @Before
    public void setUp() throws Exception {
        // view.template.publish=3
    	// mock services for testing
        handler = new AppSetupHandler();
    	mockViewService();
        myPlaceService = mock(MyPlacesServiceMybatisImpl.class);
        permissionsService = mock(PermissionsServiceIbatisImpl.class);
        mockBundleService();
        mockUserService();

        // set mocked services
        handler.setViewService(viewService);
        handler.setMyPlacesService(myPlaceService);
        handler.setPermissionsService(permissionsService);
        handler.setBundleService(bundleService);

        handler.init();
    }

    @AfterClass
    public static void teardown() {
        PropertyUtil.clearProperties();
    }

    private void mockViewService() {
        viewService = mock(ViewServiceIbatisImpl.class);
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
        bundleService = mock(BundleServiceIbatisImpl.class);
        // add all bundles needed in test
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_WHITELISTED);
        doReturn(bundle).when(bundleService).getBundleTemplateByName(BUNDLE_WHITELISTED);
    }

    private void mockUserService() throws Exception {
        userService = mock(UserService.class);
        Role role = new Role();
        role.setName("Admin");
        doReturn(role).when(userService).getRoleByName(role.getName());
        Whitebox.setInternalState(UserService.class, "instance", userService);
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
        assertNotNull("Must contain actual UUID", actualResponse.getString("uuid"));
        actualResponse.remove("uuid");
        expectedResult.remove("uuid");

        // URL will change in each run as it contains the UUID, so just checking that there is one
        assertNotNull("Must contain some URL", actualResponse.getString("url"));
        assertNotNull("URL should start with expected format", actualResponse.getString("url").startsWith("//domain.com/map?lang=fi&" + ActionConstants.PARAM_UUID + "="));
                actualResponse.remove("url");
        expectedResult.remove("url");

        assertTrue("Response should match expected", JSONHelper.isEqual(expectedResult, actualResponse));
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

        assertNull("View shouldn't have bundle that has not been whitelisted", view.getBundleByName(BUNDLE_WHITELISTED));
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

        assertNotNull("View should have bundle that has been whitelisted", view.getBundleByName(BUNDLE_WHITELISTED));
    }
	
}

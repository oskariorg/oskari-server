package fi.nls.oskari.control.view;

import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.MyPlacesServiceMybatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.view.ViewTestHelper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class PublishHandlerTest extends JSONActionRouteTest {
	
    final private PublishHandler handler = new PublishHandler();
    private ViewService viewService = null;
    private MyPlacesService myPlaceService = null;
    private PermissionsService permissionsService = null;
    private BundleService bundleService = null;

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
    	mockViewService();
        myPlaceService = mock(MyPlacesServiceMybatisImpl.class);
        permissionsService = mock(PermissionsServiceIbatisImpl.class);
        bundleService = mock(BundleServiceIbatisImpl.class);

        // set mocked services
        handler.setViewService(viewService);
        handler.setMyPlacesService(myPlaceService);
        handler.setPermissionsService(permissionsService);
        handler.setBundleService(bundleService);

     handler.init();
    }

    private void mockViewService() {
        viewService = mock(ViewServiceIbatisImpl.class);
        // add all bundles needed in test
        final View dummyView = ViewTestHelper.createMockView("framework.mapfull", "framework.infobox", "framework.publishedgrid");
        dummyView.setType(ViewTypes.USER);
        dummyView.setCreator(getLoggedInUser().getId());
        doReturn(dummyView).when(viewService).getViewWithConf(anyLong());
    }
    
    @Test
    public void testPublishFromTemplateSimpleInput() throws Exception {

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(PublishHandler.KEY_PUBDATA, ResourceHelper.readStringResource("PublishHandlerTest-input-simple.json", this));

        final ActionParameters params = createActionParams(parameters, getLoggedInUser());

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);
        final JSONObject expectedResult = ResourceHelper.readJSONResource("PublishHandlerTest-output-simple.json", this);
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
	
}

package fi.nls.oskari.control.view;

import fi.mml.map.mapwindow.service.db.MyPlacesService;
import fi.mml.map.mapwindow.service.db.MyPlacesServiceIbatisImpl;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.view.ViewTestHelper;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class PublishHandlerTest extends JSONActionRouteTest {
	
    final private PublishHandler handler = new PublishHandler();
    private ViewService viewService = null;
    private MyPlacesService myPlaceService = null;
    private PermissionsService permissionsService = null;
    private BundleService bundleService = null;

    @BeforeClass
    public static void addProperties() throws Exception {
        PropertyUtil.addProperty("view.template.publish", "3", true);
    }

    @Before
    public void setUp() throws Exception {
        // view.template.publish=3
    	// mock services for testing
    	mockViewService();
        myPlaceService = mock(MyPlacesServiceIbatisImpl.class);
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
        assertNotNull("Must contain actual UUID", actualResponse.getString("uuid"));
        actualResponse.remove("uuid");
        expectedResult.remove("uuid");
        assertTrue("Response should match expected", JSONHelper.isEqual(expectedResult, actualResponse));
    }
	
}

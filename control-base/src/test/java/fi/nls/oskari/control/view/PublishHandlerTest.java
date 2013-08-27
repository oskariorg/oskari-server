package fi.nls.oskari.control.view;

import fi.mml.map.mapwindow.service.db.MyPlacesService;
import fi.mml.map.mapwindow.service.db.MyPlacesServiceIbatisImpl;
import fi.mml.map.mapwindow.util.MapLayerWorker;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.view.modifier.param.WFSHighlightParamHandler;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.view.ViewTestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

    @Before
    public void setUp() throws Exception {
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
        doReturn(dummyView).when(viewService).getViewWithConf(anyLong());
    }
    
    @Test
    public void testEmptyViews() throws Exception {

        // mock returned views
        doReturn(Collections.emptyList()).when(viewService).getViewsForUser(anyLong());

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ViewTypes.VIEW_TYPE, ViewTypes.USER);
        
        parameters.put(PublishHandler.KEY_PUBDATA, "{'domain':'test','name':'test','language':'fi','plugins':[{'id':'Oskari.mapframework.bundle.mapmodule.plugin.Portti2Zoombar','config':{'location':{'top':'10px','left':'10px'}}},{'id':'Oskari.mapframework.mapmodule.ControlsPlugin'},{'id':'Oskari.mapframework.mapmodule.GetInfoPlugin'}],'size':{'width':700,'height':525},'mapstate':{'north':6874042,'east':517620,'zoom':1,'srs':'EPSG:3067','selectedLayers':[{'id':'base_35','opacity':100}]}}");

        final ActionParameters params = createActionParams(parameters, getLoggedInUser());
        assertEquals("Parameter is set correctly", ViewTypes.USER, params.getHttpParam(ViewTypes.VIEW_TYPE));

        verifyResponseNotWritten(params);
        handler.handleAction(params);
        // test that response was written once
        verifyResponseWritten(params);

        verifyResponseContent(ResourceHelper.readJSONResource("PublishHandlerTest-background-map.json", this));
    }
	
}

package fi.nls.oskari.control.view;

import fi.mml.map.mapwindow.service.db.LayerClassService;
import fi.mml.map.mapwindow.service.db.LayerClassServiceIbatisImpl;
import fi.mml.map.mapwindow.util.MapLayerWorker;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.view.modifier.param.WFSHighlightParamHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.domain.map.wms.LayerClass;
import fi.nls.oskari.map.data.service.PublishedMapRestrictionService;
import fi.nls.oskari.map.data.service.PublishedMapRestrictionServiceImpl;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ViewModifier;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.view.BundleTestHelper;
import fi.nls.test.view.ViewTestHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 31.5.2013
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {WFSHighlightParamHandler.class, MapLayerWorker.class, PropertyUtil.class})
public class GetAppSetupHandlerRolesFromPropertiesTest extends JSONActionRouteTest {

    final private GetAppSetupHandler handler = new GetAppSetupHandler();

    private ViewService viewService = null;
    private BundleService bundleService = null;
    private PublishedMapRestrictionService restrictionService = null;
    private PropertyUtil properties = null;

    //propertyutilsilla propertyt, checkataan että jsoniin tulee lisää bundlea.
    //

    @BeforeClass
    public static void addLocales() throws Exception {
        Properties properties = new Properties();
        try {
            properties.load(GetAppSetupHandlerRolesFromPropertiesTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
            String locales = PropertyUtil.getNecessary("oskari.locales");
            if (locales == null)
                fail("No darned locales");
        } catch (DuplicateException e) {
            fail("Should not throw exception" + e.getStackTrace());
        }
    }

    @Before
    public void setUp() throws Exception {

        mockViewService();
        mockBundleService();
        restrictionService = mock(PublishedMapRestrictionServiceImpl.class);
        mockInternalServices();



        handler.setViewService(viewService);
        handler.setBundleService(bundleService);
        handler.setPublishedMapRestrictionService(restrictionService);

        /*
        PowerMockito.mockStatic(ParamControl.class);
        doNothing().when(ParamControl.class);
        */


        try {
           PropertyUtil.addProperty("actionhandler.GetAppSetup.dynamic.bundles","admin-layerselector, admin-layerrights");
           PropertyUtil.addProperty("actionhandler.GetAppSetup.dynamic.bundle.admin-layerrights.roles", "Administrator");
           PropertyUtil.addProperty("actionhandler.GetAppSetup.dynamic.bundle.admin-layerselector.roles", "Administrator, Karttajulkaisija_Tre");
        } catch (DuplicateException e) {
                 //this method is called once for every test, duplicates don't matter.

        }

        handler.init();
    }

    @Test
    public void testAddedLayerSelectorBundle () throws Exception {
        final ActionParameters params = createActionParams(getLoggedInUser());
        Role r = new Role();
        r.setName("Karttajulkaisija_Tre");
        r.setId(42);
        params.getUser().addRole(r);
        handler.handleAction(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetAppSetupHandlerTest-view-roles-from-properties.json", this))  ;
    }



  @Test
    public void testAddedLayerRightsBundle () throws Exception {
        final ActionParameters params = createActionParams(getLoggedInUser());
        Role r = new Role();
        r.setName("Administrator");
        r.setId(66);
        params.getUser().addRole(r);
        handler.handleAction(params);

        verifyResponseContent(ResourceHelper.readJSONResource("GetAppSetupHandlerTest-view-roles-from-properties-admin.json", this))  ;
    }



    /* *********************************************
     * Service mocks
     * ********************************************
     */
    private void mockViewService() {

        viewService = mock(ViewServiceIbatisImpl.class);
        // id 2 for guest user
        doReturn(2L).when(viewService).getDefaultViewId(getGuestUser());
        // id 1 for logged in user
        doReturn(1L).when(viewService).getDefaultViewId(getLoggedInUser());
        final View dummyView = ViewTestHelper.createMockView("framework.mapfull");
        dummyView.setType(ViewTypes.DEFAULT);
        doReturn(dummyView).when(viewService).getViewWithConfByOldId(anyLong());
        doReturn(dummyView).when(viewService).getViewWithConf(anyLong());

        // TODO: mock view loading
        /**
         * fi.nls.oskari.control.ActionException: Could not get View with id: 2 and oldId: -1
         *      at fi.nls.oskari.control.view.GetAppSetupHandler.handleAction(GetAppSetupHandler.java:136)
         */
    }



    private void mockBundleService() throws Exception {

        bundleService = mock(BundleServiceIbatisImpl.class);
        doReturn(
                BundleTestHelper.loadBundle("integration.admin-layerselector")
        ).when(bundleService).getBundleTemplateByName(ViewModifier.BUNDLE_ADMINLAYERSELECTOR);

        doReturn(
                BundleTestHelper.loadBundle("framework.admin-layerrights")
        ).when(bundleService).getBundleTemplateByName(ViewModifier.BUNDLE_ADMINLAYERRIGHTS);

        doReturn(
                BundleTestHelper.loadBundle("framework.postprocessor")
        ).when(bundleService).getBundleTemplateByName(ViewModifier.BUNDLE_POSTPROCESSOR);

        // return mocked  bundle service if a new one is created (in paramhandlers for example)
        // classes doing this must be listed in PrepareForTest annotation
        whenNew(BundleServiceIbatisImpl.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return bundleService;
                    }
                });
    }

   /* private void mockPropertyUtil() throws Exception {
        final PropertyUtil properties = mock(PropertyUtil.class);

        doReturn(
                new String[]{"admin-layerselector", "admin-layerrights"}
        ).when(properties).getCommaSeparatedList("actionhandler.GetAppSetup.dynamic.bundles");


        doReturn(
                new String[]{"Administrator", "Karttajulkaisija_Tre"}
        ).when(properties).getCommaSeparatedList("actionhandler.GetAppSetup.dynamic.bundle.admin-layerselector.roles");

        doReturn(
                new String[]{"Administrator"}
        ).when(properties).getCommaSeparatedList("actionhandler.GetAppSetup.dynamic.bundle.admin-layerrights.roles");

        whenNew(PropertyUtil.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return properties;
                    }
                });

    }
*/
    private void mockInternalServices() throws Exception {

        final PermissionsService service = mock(PermissionsServiceIbatisImpl.class);
        doReturn(
                Collections.emptyList()
        ).when(service).getResourcesWithGrantedPermissions(anyString(), any(User.class), anyString());

        // return mocked  bundle service if a new one is created (in paramhandlers for example)
        // classes doing this must be listed in PrepareForTest annotation
        whenNew(PermissionsServiceIbatisImpl.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return service;
                    }
                });


/*
    public static JSONObject getSelectedLayersStructure(List<String> layerList,
                                                        User user, String lang, String remoteIp, boolean isPublished) {
                                                        */
        // TODO: mock MapLayerWorker.getSelectedLayersStructure() instead to return a valid JSON structure
        final LayerClassService layerClassService = mock(LayerClassServiceIbatisImpl.class);
        LayerClass layerClass = mock(LayerClass.class);
        doReturn(
                layerClass
        ).when(layerClassService).findOrganizationalStructureByClassId(anyInt());

        // return mocked  bundle service if a new one is created (in paramhandlers for example)
        // classes doing this must be listed in PrepareForTest annotation
        whenNew(LayerClassServiceIbatisImpl.class).withNoArguments().
                thenAnswer(new Answer<Object>() {
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return layerClassService;
                    }
                });
    }

}

package fi.nls.oskari.control.view;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.DataProviderServiceMybatisImpl;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ViewModifier;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.TestHelper;
import fi.nls.test.view.BundleTestHelper;
import fi.nls.test.view.ViewTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;
import org.oskari.permissions.model.ResourceType;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 31.5.2013
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
@ExtendWith(MockitoExtension.class)
public class GetAppSetupHandlerRolesFromPropertiesTest extends JSONActionRouteTest {

    final private GetAppSetupHandler handler = new GetAppSetupHandler();

    private ViewService viewService = null;
    private BundleService bundleService = null;

    private MockedConstruction<PermissionServiceMybatisImpl> permissionServiceMybatisMockedConstruction;
    private MockedConstruction<DataProviderServiceMybatisImpl> dataProviderServiceMybatisMockedConstruction;
    private MockedConstruction<BundleServiceMybatisImpl> bundleServiceMybatisMockedConstruction;

    @BeforeAll
    public static void addLocales() throws Exception {
        TestHelper.registerTestDataSource();
        Properties properties = new Properties();
        try {
            properties.load(GetAppSetupHandlerRolesFromPropertiesTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
            PropertyUtil.addProperty("oskari.user.service", "fi.nls.oskari.service.DummyUserService", true);
            PropertyUtil.getNecessary("oskari.locales");
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

        try {
           PropertyUtil.addProperty("actionhandler.GetAppSetup.dynamic.bundles","admin-layerselector, admin-layerrights");
           PropertyUtil.addProperty("actionhandler.GetAppSetup.dynamic.bundle.admin-layerrights.roles", "Administrator");
           PropertyUtil.addProperty("actionhandler.GetAppSetup.dynamic.bundle.admin-layerselector.roles", "Administrator, Karttajulkaisija_Tre");
        } catch (DuplicateException e) {
                 //this method is called once for every test, duplicates don't matter.
        }

        handler.init();
    }

    @AfterEach
    public void teardownMockedContructors() {
        if (permissionServiceMybatisMockedConstruction != null) {
            permissionServiceMybatisMockedConstruction.close();
        }
        if (dataProviderServiceMybatisMockedConstruction != null) {
            dataProviderServiceMybatisMockedConstruction.close();
        }

        if (bundleServiceMybatisMockedConstruction != null) {
            bundleServiceMybatisMockedConstruction.close();
        }
    }
    @AfterAll
    public static void teardown() {
        PropertyUtil.clearProperties();
        // To get fresh start for components
        OskariComponentManager.teardown();
        TestHelper.teardown();
    }

    @Test
    public void testAddedLayerSelectorBundle () throws Exception {

    	final ActionParameters params = createActionParams(getLoggedInUser());
        Role r = new Role();
        r.setName("Karttajulkaisija_Tre");
        r.setId(42);
        params.getUser().addRole(r);
        handler.handleAction(params);
        GetAppSetupTestHelper.verifyResponseContent("GetAppSetupHandlerTest-view-roles-from-properties.json", getResponseJSON());
    }



    @Test
    public void testAddedLayerRightsBundle () throws Exception {
        final ActionParameters params = createActionParams(getLoggedInUser());
        Role r = new Role();
        r.setName("Administrator");
        r.setId(66);
        params.getUser().addRole(r);
        handler.handleAction(params);
        GetAppSetupTestHelper.verifyResponseContent("GetAppSetupHandlerTest-view-roles-from-properties-admin.json", getResponseJSON());
    }



    /* *********************************************
     * Service mocks
     * ********************************************
     */
    private void mockViewService() {

        viewService = mock(AppSetupServiceMybatisImpl.class);
        // id 2 for guest user
        Mockito.lenient().doReturn(2L).when(viewService).getDefaultViewId(getGuestUser());
        // id 1 for logged in user
        doReturn(1L).when(viewService).getDefaultViewId(getLoggedInUser());
        final View dummyView = ViewTestHelper.createMockView("framework.mapfull");
        dummyView.setType(ViewTypes.DEFAULT);
        Mockito.lenient().doReturn(dummyView).when(viewService).getViewWithConfByOldId(anyLong());
        Mockito.lenient().doReturn(dummyView).when(viewService).getViewWithConf(anyLong());
        Mockito.lenient().doReturn(dummyView).when(viewService).getViewWithConfByUuId(anyString());

        // TODO: mock view loading
        /**
         * fi.nls.oskari.control.ActionException: Could not get View with id: 2 and oldId: -1
         *      at fi.nls.oskari.control.view.GetAppSetupHandler.handleAction(GetAppSetupHandler.java:136)
         */
    }


    private void mockBundleService() throws Exception {

        bundleService = mock(BundleServiceMybatisImpl.class);
        // return mocked bundle service if a new one is created (in paramhandlers for example) excluding parameterized constructors
        bundleServiceMybatisMockedConstruction = Mockito.mockConstruction(BundleServiceMybatisImpl.class);

        doReturn(
                BundleTestHelper.loadBundle("integration.admin-layerselector")
        ).when(bundleService).getBundleTemplateByName(ViewModifier.BUNDLE_ADMINLAYERSELECTOR);

        doReturn(
                BundleTestHelper.loadBundle("framework.admin-layerrights")
        ).when(bundleService).getBundleTemplateByName(ViewModifier.BUNDLE_ADMINLAYERRIGHTS);

        Mockito.lenient().doReturn(
                BundleTestHelper.loadBundle("framework.postprocessor")
        ).when(bundleService).getBundleTemplateByName(ViewModifier.BUNDLE_POSTPROCESSOR);
    }

    private void mockInternalServices() throws Exception {

        final PermissionService service = mock(PermissionServiceMybatisImpl.class);
        // permission check is skipped here so just mock the call
        Mockito.lenient().doReturn(Optional.empty()).when(service).findResource(eq(ResourceType.maplayer.name()), any(String.class));

        // return mocked bundle service if a new one is created (in paramhandlers for example) excluding parameterized constructors
        permissionServiceMybatisMockedConstruction = Mockito.mockConstruction(PermissionServiceMybatisImpl.class);

        // TODO: mock MapLayerWorker.getSelectedLayersStructure() instead to return a valid JSON structure
        final DataProviderService groupService = mock(DataProviderServiceMybatisImpl.class);
        DataProvider group = mock(DataProvider.class);
        group.setName("en", "Testing");
        Mockito.lenient().doReturn(group).when(groupService).find(anyInt());
        Mockito.lenient().doReturn(Collections.emptyList()).when(groupService).findAll();

        // return mocked bundle service if a new one is created (in paramhandlers for example) excluding parameterized constructors
        dataProviderServiceMybatisMockedConstruction = Mockito.mockConstruction(DataProviderServiceMybatisImpl.class);
    }
}

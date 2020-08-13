package fi.nls.oskari.control.admin;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.HashSet;

import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oskari.permissions.PermissionService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OskariComponentManager.class})
public class LayerAdminMetadataHandlerTest extends JSONActionRouteTest {
    
    final private static LayerAdminMetadataHandler handler = new LayerAdminMetadataHandler();
    
    @BeforeClass
    public static void setup() throws Exception {
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getCanonicalName(), true);
        setupPermissionsServiceMock();
        handler.init();
    }
    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }

    private static void setupPermissionsServiceMock() {
        PermissionService permissionService = mock(PermissionService.class);
        PowerMockito.mockStatic(OskariComponentManager.class);
        when(OskariComponentManager.getComponentOfType(PermissionService.class)).thenReturn(permissionService);
        
        doReturn(new HashSet<String>()).when(permissionService).getAdditionalPermissions();
        doAnswer(invocation -> invocation.getArgument(0)).when(permissionService).getPermissionName(anyString(),anyString());
    }
    
    @Test
    public void testGetWithAdmin() throws Exception {
        final ActionParameters params = createActionParams(getAdminUser());
        handler.handleAction(params);
        verifyResponseContent(ResourceHelper.readJSONResource("LayerAdminMetadataHandlerTests-expected.json", this));
    }
    
    
    @Test(expected = ActionDeniedException.class)
    public void testGetWithGuest() throws Exception {
        final ActionParameters params = createActionParams(getGuestUser());
        handler.handleAction(params);
    }
    
    @Test(expected = ActionDeniedException.class)
    public void testGetWithLoggedInNonAdminUser() throws Exception {
        final ActionParameters params = createActionParams(getLoggedInUser());
        handler.handleAction(params);
    }
}

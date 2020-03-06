package fi.nls.oskari.control.admin;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;

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
@PrepareForTest({UserService.class, OskariComponentManager.class})
public class LayerAdminMetadataHandlerTest extends JSONActionRouteTest {
    
    final private static LayerAdminMetadataHandler handler = new LayerAdminMetadataHandler();
    
    @BeforeClass
    public static void setup() throws ServiceException {
        
        setupUserServiceMock();
        setupPermissionsServiceMock();
        handler.init();
    }
    private static void setupUserServiceMock() throws ServiceException {
        
        UserService userService = mock(UserService.class);
        PowerMockito.mockStatic(UserService.class);
        when(UserService.getInstance()).thenReturn(userService);
        
        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("Guest");
        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("User");
        Role role3 = new Role();
        role3.setId(3L);
        role3.setName("Admin");
        
        Role[] mockRoles = new Role[]{role1,role2,role3};
        
        doReturn(mockRoles).when(userService).getRoles();
        doReturn(Role.getAdminRole()).when(userService).getRoleByName("Admin");

    }
    private static void setupPermissionsServiceMock() throws ServiceException {
        PermissionService permissionService = mock(PermissionService.class);
        PowerMockito.mockStatic(OskariComponentManager.class);
        when(OskariComponentManager.getComponentOfType(PermissionService.class)).thenReturn(permissionService);
        
        doReturn(new HashSet<String>()).when(permissionService).getAdditionalPermissions();
        when(permissionService.getPermissionName(anyString(),anyString())).thenAnswer(invocation -> { return (String) invocation.getArgument(0); });
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

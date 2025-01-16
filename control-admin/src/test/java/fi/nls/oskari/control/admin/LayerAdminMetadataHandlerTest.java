package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oskari.permissions.PermissionService;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LayerAdminMetadataHandlerTest extends JSONActionRouteTest {
    
    final private static LayerAdminMetadataHandler handler = new LayerAdminMetadataHandler();
    
    @BeforeAll
    public static void setup() throws Exception {
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getCanonicalName(), true);
        setupPermissionsServiceMock();
        TestHelper.registerTestDataSource();
        handler.init();
    }
    @AfterAll
    public static void tearDown() {
        PropertyUtil.clearProperties();
        TestHelper.teardown();
    }

    private static void setupPermissionsServiceMock() {
        PermissionService permissionService = mock(PermissionService.class);
        Mockito.mockStatic(OskariComponentManager.class);
        when(OskariComponentManager.getComponentOfType(PermissionService.class)).thenReturn(permissionService);
        
        doReturn(new HashSet<String>()).when(permissionService).getAdditionalPermissions();
        doAnswer(invocation -> invocation.getArgument(0)).when(permissionService).getPermissionName(anyString(),anyString());
    }
    
    @Test
    @Disabled
    // java.lang.NullPointerException: Cannot invoke "org.json.JSONObject.toString(int)" because "actualResponse" is null
    public void testGetWithAdmin() throws Exception {
        final ActionParameters params = createActionParams(getAdminUser());
        handler.handleAction(params);
        verifyResponseContent(ResourceHelper.readJSONResource("LayerAdminMetadataHandlerTests-expected.json", this));
    }
    
    
    @Test()
    public void testGetWithGuest() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            final ActionParameters params = createActionParams(getGuestUser());
            handler.handleAction(params);
        });
    }
    
    @Test()
    public void testGetWithLoggedInNonAdminUser() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            final ActionParameters params = createActionParams(getLoggedInUser());
            handler.handleAction(params);
        });
    }
}

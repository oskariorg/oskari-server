package fi.nls.oskari.control;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple tests for ActionParameters.require<LoggedIn|Admin>User()
 */
public class ActionParametersTest {

    @BeforeEach
    public void setup() throws Exception {
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getName(), true);
    }
    @AfterEach
    public void teardown() throws Exception {
        PropertyUtil.clearProperties();
    }
    private User getGuestUser() throws Exception {
        return UserService.getInstance().getGuestUser();
    }

    private User getLoggedInUser() {
        return new User();
    }

    private User getAdminUser() {
        User user = getLoggedInUser();
        System.out.println(Role.getAdminRole());
        user.addRole(Role.getAdminRole());
        return user;
    }

    @Test()
    public void testRequireLoggedInUserWithGuest() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            ActionParameters params = new ActionParameters();
            params.setUser(getGuestUser());
            params.requireLoggedInUser();
        });
    }
    @Test
    public void testRequireLoggedInUserWithUser() throws Exception {
        ActionParameters params = new ActionParameters();
        params.setUser(getLoggedInUser());
        params.requireLoggedInUser();
    }

    @Test()
    public void testRequireAdminUserWithGuest() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            ActionParameters params = new ActionParameters();
            params.setUser(getGuestUser());
            params.requireAdminUser();
        });
    }
    @Test()
    public void testRequireAdminUserWithUser() throws Exception {
        assertThrows(ActionDeniedException.class, () -> {
            ActionParameters params = new ActionParameters();
            params.setUser(getLoggedInUser());
            params.requireAdminUser();
        });
    }
    @Test
    @Disabled("FIXME: mock UserService.getInstance().getRoles() -> return atleast Role.getAdminRole()")
    public void testRequireAdminUserWithAdmin() throws Exception {
        ActionParameters params = new ActionParameters();
        params.setUser(getAdminUser());
        assertTrue(params.getUser().hasRoleWithId(Role.getAdminRole().getId()));
        params.requireLoggedInUser();
        params.requireAdminUser();
    }
}

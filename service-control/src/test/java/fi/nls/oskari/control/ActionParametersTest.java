package fi.nls.oskari.control;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Simple tests for ActionParameters.require<LoggedIn|Admin>User()
 */
public class ActionParametersTest {

    @Before
    public void setup() throws Exception {
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getName(), true);
    }
    @After
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

    @Test(expected = ActionDeniedException.class)
    public void testRequireLoggedInUserWithGuest() throws Exception {
        ActionParameters params = new ActionParameters();
        params.setUser(getGuestUser());
        params.requireLoggedInUser();
    }
    @Test
    public void testRequireLoggedInUserWithUser() throws Exception {
        ActionParameters params = new ActionParameters();
        params.setUser(getLoggedInUser());
        params.requireLoggedInUser();
    }

    @Test(expected = ActionDeniedException.class)
    public void testRequireAdminUserWithGuest() throws Exception {
        ActionParameters params = new ActionParameters();
        params.setUser(getGuestUser());
        params.requireAdminUser();
    }
    @Test(expected = ActionDeniedException.class)
    public void testRequireAdminUserWithUser() throws Exception {
        ActionParameters params = new ActionParameters();
        params.setUser(getLoggedInUser());
        params.requireAdminUser();
    }
    @Test
    @Ignore("FIXME: mock UserService.getInstance().getRoles() -> return atleast Role.getAdminRole()")
    public void testRequireAdminUserWithAdmin() throws Exception {
        ActionParameters params = new ActionParameters();
        params.setUser(getAdminUser());
        assertTrue(params.getUser().hasRoleWithId(Role.getAdminRole().getId()));
        params.requireLoggedInUser();
        params.requireAdminUser();
    }
}

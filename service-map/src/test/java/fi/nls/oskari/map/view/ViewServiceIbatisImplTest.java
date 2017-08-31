package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ViewServiceIbatisImplTest {

    private ViewService service;

    @Before
    public void setUp() throws Exception {
        /*
        view.default=[global default view id that is used if role-based default view is not found]
        view.default.roles=[comma-separated list of role names in descending order f.ex. Admin,User,Guest]
        view.default.[role name]=[default view id for the role]
        */
        PropertyUtil.addProperty("view.default", "5");
        PropertyUtil.addProperty("view.default.Admin", "2");
        PropertyUtil.addProperty("view.default.User", "3");
        PropertyUtil.addProperty("view.default.Guest", "4");
        PropertyUtil.addProperty("view.default.roles", "Admin, User, Guest");
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getCanonicalName(), true);

        service = new ViewServiceIbatisImpl();
    }

    @Test
    public void testGetDefaultViewId() throws Exception {

        final User guest = new GuestUser();
        assertEquals("User with no roles should end up using the value configure in 'view.default'", 5, service.getDefaultViewId(guest));
        guest.addRole(1, "Guest");
        assertEquals("User with Guest role should end up using the value configure in 'view.default.Guest'", 4, service.getDefaultViewId(guest));

        final User user = new User();
        user.addRole(2, "User");
        assertEquals("User with User role should end up using the value configure in 'view.default.User'", 3, service.getDefaultViewId(user));
        user.addRole(3, "Admin");
        assertEquals("User with Admin role should end up using the value configure in 'view.default.Admin'", 2, service.getDefaultViewId(user));

    }

    @After
    public void tearDown() {
        PropertyUtil.clearProperties();
    }
}

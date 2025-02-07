package fi.nls.oskari.map.view;

import org.oskari.user.GuestUser;
import org.oskari.user.User;
import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

public class AppSetupServiceMybatisImplTest {

    private ViewService service;

    @BeforeEach
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
        DataSource ds = TestHelper.createMemDBforUnitTest();

        service = new AppSetupServiceMybatisImpl(ds);
    }

    @Test
    public void testGetDefaultViewId() throws Exception {

        final User guest = new GuestUser();
        Assertions.assertEquals(5, service.getDefaultViewId(guest), "User with no roles should end up using the value configure in 'view.default'");
        guest.addRole(1, "Guest");
        Assertions.assertEquals(4, service.getDefaultViewId(guest), "User with Guest role should end up using the value configure in 'view.default.Guest'");

        final User user = new User();
        user.addRole(2, "User");
        Assertions.assertEquals(3, service.getDefaultViewId(user), "User with User role should end up using the value configure in 'view.default.User'");
        user.addRole(3, "Admin");
        Assertions.assertEquals(2, service.getDefaultViewId(user), "User with Admin role should end up using the value configure in 'view.default.Admin'");

    }

    @AfterEach
    public void tearDown() {
        PropertyUtil.clearProperties();
    }
}

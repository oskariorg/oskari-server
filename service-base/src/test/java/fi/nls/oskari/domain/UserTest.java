package fi.nls.oskari.domain;

import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author SMAKINEN
 */
public class UserTest {

    @Before
    public void setup() throws Exception {
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getCanonicalName(), true);
    }

    @Test
    public void testHasRole() {

        User user = new User();
        user.addRole(1, "testrole");

        assertTrue("User should have role 'testrole'", user.hasRole("testrole"));
        assertFalse("User should NOT have role 'dummyrole'", user.hasRole("dummyrole"));

        user.addRole(2, "testrole 2");
        assertTrue("User should have role 'testrole 2'", user.hasRole("testrole 2"));
        assertFalse("User should NOT have role 'dummyrole'", user.hasRole("dummyrole"));

        String[] rolesArray = new String[] { "role 1", "role 2"};
        assertFalse("User should NOT have any role in given array", user.hasAnyRoleIn(rolesArray));

        user.addRole(3, "role 2");
        assertTrue("User should have role listed in given array", user.hasAnyRoleIn(rolesArray));

        user.addRole(4, Role.getAdminRole().getName());
        assertTrue("Admin user should have any role", user.hasRole("any role what so ever"));
    }

    @After
    public void tearDown() {
        PropertyUtil.clearProperties();
    }

}

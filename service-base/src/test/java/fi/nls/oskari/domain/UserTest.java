package fi.nls.oskari.domain;

import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author SMAKINEN
 */
public class UserTest {

    @BeforeEach
    public void setup() throws Exception {
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getCanonicalName(), true);
    }

    @Test
    public void testHasRole() {

        User user = new User();
        user.addRole(1, "testrole");

        Assertions.assertTrue(user.hasRole("testrole"), "User should have role 'testrole'");
        Assertions.assertFalse(user.hasRole("dummyrole"), "User should NOT have role 'dummyrole'");

        user.addRole(2, "testrole 2");
        Assertions.assertTrue(user.hasRole("testrole 2"), "User should have role 'testrole 2'");
        Assertions.assertFalse(user.hasRole("dummyrole"), "User should NOT have role 'dummyrole'");

        String[] rolesArray = new String[] { "role 1", "role 2"};
        Assertions.assertFalse(user.hasAnyRoleIn(rolesArray), "User should NOT have any role in given array");

        user.addRole(3, "role 2");
        Assertions.assertTrue(user.hasAnyRoleIn(rolesArray), "User should have role listed in given array");

        user.addRole(4, Role.getAdminRole().getName());
        Assertions.assertTrue(user.hasRole("any role what so ever"), "Admin user should have any role");
    }

    @AfterEach
    public void tearDown() {
        PropertyUtil.clearProperties();
    }

}

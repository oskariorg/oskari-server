package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.fail;

/**
 * Simple user-credentials check for handlers listed in getAdminHandlers().
 */
@RunWith(Parameterized.class)
public class UserCredentialsTest extends JSONActionRouteTest {

    private ActionHandler handler = null;

    @Before
    public void initPropertiese() {
        Properties properties = new Properties();
        try {
            properties.load(UserCredentialsTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
        } catch (Exception e) {
            //fail("Should not throw exception" + e.getStackTrace());
        }
    }

    @Parameterized.Parameters
    public static Collection getAdminHandlers() {
        // testing the same thing for all these admin routes
        return Arrays.asList(new Object[][]{
                {CacheHandler.class},
                {ManageRolesHandler.class},
                {SystemViewsHandler.class},
                {UsersHandler.class}
        });
    }

    public UserCredentialsTest(Class<ActionHandler> clazz) {
        try {
            handler = clazz.newInstance();
            handler.init();
        } catch (Exception ignored) {}
    }

    @Test(expected = ActionDeniedException.class)
    public void testWithGuest() throws Exception {
        handler.handleAction(createActionParams());
        fail("ActionDeniedException should have been thrown. ActionHandler: " + handler.getClass().getName());
    }

    @Test(expected = ActionDeniedException.class)
    public void testWithUser() throws Exception {
        handler.handleAction(createActionParams(getLoggedInUser()));
        fail("ActionDeniedException should have been thrown. ActionHandler: " + handler.getClass().getName());
    }
    @Test
    public void testWithAdmin() throws Exception {
        System.out.println("Parameterized ActionHandler is : " + handler.getClass().getName());
        handler.handleAction(createActionParams(getAdminUser()));
        // no exception thrown so we are happy
    }
}

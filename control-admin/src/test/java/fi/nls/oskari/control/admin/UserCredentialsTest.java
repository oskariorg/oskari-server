package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.service.DummyUserService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.TestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Simple user-credentials check for handlers listed in getAdminHandlers().
 */
public class UserCredentialsTest extends JSONActionRouteTest {

    //private ActionHandler handler = null;

    @BeforeAll
    public static void setup() throws Exception {
        TestHelper.registerTestDataSource();
        PropertyUtil.addProperty("oskari.user.service", DummyUserService.class.getCanonicalName(), true);
        // So ViewsHandler doesn't try to connect to db
        PropertyUtil.addProperty("view.default", "1", true);
    }
    @AfterAll
    public static void tearDown() {
        PropertyUtil.clearProperties();
        OskariComponentManager.teardown();
        TestHelper.teardown();
    }


    public static Stream<Arguments> getAdminHandlers() {
        return Stream.of(
                Arguments.of(CacheHandler.class),
                Arguments.of(ManageRolesHandler.class),
                Arguments.of(SystemViewsHandler.class),
                Arguments.of(UsersHandler.class),
                Arguments.of(LayerAdminMetadataHandler.class)
        );
    }

    @ParameterizedTest
    @MethodSource("getAdminHandlers")
    public void testWithGuest(Class<ActionHandler> clazz) throws Exception {
        ActionHandler handler = clazz.newInstance();
        handler.init();
        assertThrows(ActionDeniedException.class, () -> {
            handler.handleAction(createActionParams());
        });
    }

    @ParameterizedTest
    @MethodSource("getAdminHandlers")
    public void testWithUser(Class<ActionHandler> clazz) throws Exception {
        ActionHandler handler = clazz.newInstance();
        handler.init();
        assertThrows(ActionDeniedException.class, () -> {
            handler.handleAction(createActionParams(getLoggedInUser()));
        });
    }
    @ParameterizedTest
    @MethodSource("getAdminHandlers")
    public void testWithAdmin(Class<ActionHandler> clazz) throws Exception {
        ActionHandler handler = clazz.newInstance();
        handler.init();
        System.out.println("Parameterized ActionHandler is : " + handler.getClass().getName());
        handler.handleAction(createActionParams(getAdminUser()));
        // no exception thrown so we are happy
    }
}

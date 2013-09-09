package fi.nls.oskari.control.layer;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.GetWMSCapabilities;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;


/**
 * @author SMAKINEN
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {GetWMSCapabilities.class})
public class GetWSCapabilitiesHandlerTest extends JSONActionRouteTest {

    final private  GetWSCapabilitiesHandler handler = new  GetWSCapabilitiesHandler();

    @Before
    public void setUp() throws Exception {
        handler.init();
    }
    @Test(expected = ActionParamsException.class)
    public void testHandleActionInvalidParams() throws Exception {

        final ActionParameters params = createActionParams();
        handler.handleAction(params);

        throw new IllegalStateException("Should not get his far without wmsurl parameter");
    }

    @Test(expected = ActionDeniedException.class)
    public void testHandleActionGuestUser() throws Exception {

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("wmsurl", "dummyurl");

        final ActionParameters params = createActionParams(parameters);
        handler.handleAction(params);

        throw new IllegalStateException("Should not get his far with guest user");
    }

    @Test(expected = ActionDeniedException.class)
    public void testHandleActionInvalidUser() throws Exception {

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("wmsurl", "dummyurl");
        final User user = new User();
        user.addRole(1, "test role");
        final ActionParameters params = createActionParams(parameters);
        handler.handleAction(params);

        throw new IllegalStateException("Should not get his far with user without configured roles");
    }

    @Test
    public void testHandleActionWithConfiguredRole() throws Exception {

        // users with 'test role' or 'my role' should have access
        PropertyUtil.addProperty("actionhandler.GetWSCapabilitiesHandler.roles", "test role, my role");
        // re-init to get updated property
        handler.init();

        // setup params
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("wmsurl", "dummyurl");
        final User user = new User();
        user.addRole(1, "test role");
        final ActionParameters params = createActionParams(parameters, user);
        try {
            handler.handleAction(params);
        }
        catch(ActionException ex) {
            // FIXME: we should mock the static class and do some better handling for this
            // anyway we get close enough for now by checking for MalformedURLException...
            // PowerMockito.mockStatic(GetWMSCapabilities.class);
            // when(GetWMSCapabilities.getResponse(anyString())).thenReturn("Hello!");
            assertTrue(ex.getCause() instanceof MalformedURLException);
        }
    }
}

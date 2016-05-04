package fi.nls.oskari.control.routing;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import static fi.nls.oskari.control.ActionConstants.*;

@Ignore
public class RoutingHandlerTest extends JSONActionRouteTest {

    RoutingHandler routingHandler = new RoutingHandler();
    @Test
    public void testHandleAction() throws Exception {
        final Map<String, String> parameters = new HashMap<String, String>();

        //requires following properties to work:
        PropertyUtil.addProperty("routing.url", "");
        PropertyUtil.addProperty("routing.user", "");
        PropertyUtil.addProperty("routing.password", "");
        PropertyUtil.addProperty("routing.srs", "");

        System.setProperty("http.proxyHost", "");
        System.setProperty("https.proxyHost", "");
        System.setProperty("http.proxyPort", "");
        System.setProperty("https.proxyPort", "");

        parameters.put("fromlon", "381210");
        parameters.put("fromlat", "6679422");
        parameters.put("tolat", "6671022");
        parameters.put("tolon", "385010");
        parameters.put(PARAM_SRS, "EPSG:3067");
        final ActionParameters params = createActionParams(parameters, getLoggedInUser());
        routingHandler.handleAction(params);
        System.out.println(getResponseString());
    }
}
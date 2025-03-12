package fi.nls.oskari.control.routing;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.routing.RouteResponse;
import fi.nls.oskari.routing.RoutingServiceOpenTripPlannerImpl;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.oskari.user.User;

import java.util.HashMap;
import java.util.Map;

import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class RoutingHandlerTest extends JSONActionRouteTest {

    private static RoutingHandler routingHandler = null;

    private static RoutingServiceOpenTripPlannerImpl routingService = null;

    private static MockedConstruction<RoutingServiceOpenTripPlannerImpl> mockRoutingService;
    private static MockedConstruction<User> mockUser;
    @BeforeAll
    private static void init() throws DuplicateException {
        //requires following properties to work:
        PropertyUtil.addProperty("routing.url", "");
        PropertyUtil.addProperty("routing.user", "");
        PropertyUtil.addProperty("routing.password", "");
        PropertyUtil.addProperty("routing.srs", "EPSG:3067");

        System.setProperty("http.proxyHost", "");
        System.setProperty("https.proxyHost", "");
        System.setProperty("http.proxyPort", "");
        System.setProperty("https.proxyPort", "");
        System.setProperty("oskari.user.service", "");

        RouteResponse mockRouteResponse = mock(RouteResponse.class);
        mockRoutingService = mockConstruction(RoutingServiceOpenTripPlannerImpl.class,
            (mock, context) -> {
                when(mock.getRoute(any())).thenReturn(mockRouteResponse);
        });

        mockUser = mockConstruction(User.class, (mock, context) -> {
            when(mock.isAdmin()).thenReturn(false);
        });
        routingService = mock(RoutingServiceOpenTripPlannerImpl.class);
        when(routingService.getRoute(any())).thenReturn(null);
        routingHandler = new RoutingHandler();
    }

    @AfterAll
    private static void destroy() {
        PropertyUtil.clearProperties();
        if (mockRoutingService != null) {
            mockRoutingService.close();
        }
        if (mockUser != null) {
            mockUser.close();
        }

    }

    private Map<String, String> getParameters() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("fromlon", "381210");
        parameters.put("fromlat", "6679422");
        parameters.put("tolat", "6671022");
        parameters.put("tolon", "385010");
        parameters.put(PARAM_SRS, "EPSG:3067");
        parameters.put("routing.srs", "EPSG:4326");
        return parameters;

    }
    @Test
    public void testHandleAction() throws Exception {
        final Map<String, String> parameters = getParameters();
        final ActionParameters params = createActionParams(parameters, getLoggedInUser());
        Assertions.assertDoesNotThrow(() -> routingHandler.handleAction(params));
    }

    @Test
    public void testHandleActionDateCorrectFormat() throws ActionException, DuplicateException {
        final Map<String, String> parameters = getParameters();

        parameters.put("date", "20211221");
        parameters.put("time", "13:23");

        ActionParameters actionParams = createActionParams(parameters);
        when(routingService.getRoute(any())).thenReturn(null);
        Assertions.assertDoesNotThrow(() -> routingHandler.handleAction(actionParams));
    }

    @Test
    public void testHandleActionDateCorrectFormatTimeSingleDigit() throws ActionException, DuplicateException {
        final Map<String, String> parameters = getParameters();
        parameters.put("date", "20211221");
        parameters.put("time", "1:23");

        ActionParameters actionParams = createActionParams(parameters);
        Assertions.assertDoesNotThrow(() -> routingHandler.handleAction(actionParams));
    }

    @Test
    public void testHandleActionDateCorrectFormatNoTime() throws ActionException, DuplicateException {
        final Map<String, String> parameters = getParameters();
        parameters.put("date", "20211221");
        ActionParameters actionParams = createActionParams(parameters);
        Assertions.assertDoesNotThrow(() -> routingHandler.handleAction(actionParams));
    }

    @Test
    public void testHandleActionNoDateTimeCorrectFormat() throws ActionException, DuplicateException {
        final Map<String, String> parameters = getParameters();
        parameters.put("time", "12:22");

        ActionParameters actionParams = createActionParams(parameters);
        Assertions.assertDoesNotThrow(() -> routingHandler.handleAction(actionParams));
    }

    @Test
    public void testHandleActionDateWrongFormat() throws ActionException {
        final Map<String, String> parameters = getParameters();
        parameters.put("date", "21.12.2021");
        parameters.put("time", "13:23:24");

        ActionParamsException ex = Assertions.assertThrows(ActionParamsException.class, () -> routingHandler.handleAction(createActionParams(parameters)));
        Assertions.assertEquals("Couldn't parse date", ex.getMessage());
    }
}
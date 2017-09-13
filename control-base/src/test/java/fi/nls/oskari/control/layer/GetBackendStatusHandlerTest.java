package fi.nls.oskari.control.layer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.test.control.JSONActionRouteTest;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.Test;
import org.oskari.service.backendstatus.BackendStatusService;

public class GetBackendStatusHandlerTest extends JSONActionRouteTest {

    private GetBackendStatusHandler handler;

    @Before
    public void init() {
        BackendStatus foo = new BackendStatus(1, "OK", null, "http://foo.bar/12345");
        BackendStatus bar = new BackendStatus(2, "ERROR", "Unknown service", null);
        BackendStatus baz = new BackendStatus(3, "UNKNOWN", "stale", null);
        BackendStatus qux = new BackendStatus(4, "DOWN", null, "http://foo.bar/54321");
        List<BackendStatus> all = Arrays.asList(foo, bar, baz, qux);
        List<BackendStatus> alert = Arrays.asList(qux);
        BackendStatusService mock = mock(BackendStatusService.class);
        when(mock.findAll()).thenReturn(all);
        when(mock.findAllWithAlert()).thenReturn(alert);
        handler = new GetBackendStatusHandler(mock, new ObjectMapper());
    }

    @Test
    public void whenStatusIsErrorAndStatusMessageStartsWithUnknownShouldBeChangedToUnknownInResponse()
            throws JsonProcessingException, UnsupportedEncodingException, ActionException {
        ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(GetBackendStatusHandler.PARAM_SUBSET, GetBackendStatusHandler.SUBSET_ALL_KNOWN);
        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest("GET", queryParams));
        params.setResponse(mockHttpServletResponse(responseBody));
        handler.handleAction(params);
        String actual = responseBody.toString("UTF-8");

        String expected = "{'backendstatus':["
                + "{'maplayer_id':1,'status':'OK','statusjson':null,'infourl':'http://foo.bar/12345','ts':null},"
                + "{'maplayer_id':2,'status':'UNKNOWN','statusjson':null,'infourl':null,'ts':null},"
                + "{'maplayer_id':3,'status':'UNKNOWN','statusjson':'stale','infourl':null,'ts':null},"
                + "{'maplayer_id':4,'status':'DOWN','statusjson':null,'infourl':'http://foo.bar/54321','ts':null}"
                + "]}";
        expected = expected.replace('\'', '"');
        assertEquals(expected, actual);
    }

    @Test
    public void whenNoParamsSpecifiedReturnsOnlyAlerts()
            throws JsonProcessingException, UnsupportedEncodingException, ActionException {
        ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest());
        params.setResponse(mockHttpServletResponse(responseBody));
        handler.handleAction(params);
        String actual = responseBody.toString("UTF-8");

        String expected = "{'backendstatus':["
                + "{'maplayer_id':4,'status':'DOWN','statusjson':null,'infourl':'http://foo.bar/54321','ts':null}"
                + "]}";
        expected = expected.replace('\'', '"');
        assertEquals(expected, actual);
    }

}

package fi.nls.test.control;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.JSONTestHelper;
import fi.nls.test.util.MapBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**

 * @author SMAKINEN
 * Base junit test class for ActionHandlers responding with JSON. Convenience methods for mocking ActionParams and getting the written response
 */
public class JSONActionRouteTest {

    private StringWriter response = new StringWriter();

    @BeforeEach
    public void jsonActionRouteSetUp() throws Exception {
        response = new StringWriter();
    }

    @AfterEach
    public void jsonActionRouteTeardown() throws Exception {
        response.close();
    }

    /**
     * Creates an empty ActionParams with no http parameters and GuestUser
     * @return
     */
    public ActionParameters createActionParams() {
        return createActionParams(getGuestUser());
    }

    public ActionParameters createActionParams(final InputStream payload) {
        return createActionParams(getGuestUser(), payload);
    }

    /**
     * Creates an empty ActionParams with no http parameters and given User
     * @return
     */
    public ActionParameters createActionParams(final User user) {
        return createActionParams(Collections.emptyMap(), user);
    }

    public ActionParameters createActionParams(final User user, final InputStream payload) {
        return createActionParams(Collections.emptyMap(), user, payload);
    }

    /**
     * Creates an ActionParams with given http parameters and GuestUser
     * @return
     */
    public ActionParameters createActionParams(final Map<String, String> parameters) {
        return createActionParams(parameters, getGuestUser());
    }

    public ActionParameters createActionParams(final Map<String, String> parameters, final InputStream payload) {
        return createActionParams(parameters, getGuestUser(), payload);
    }

    /**
     * Creates an ActionParams with given http parameters and User
     * @return
     */
    public ActionParameters createActionParams(final Map<String, String> parameters, final User user) {
        return createActionParams(parameters, user, null);
    }
    public ActionParameters createActionParams(final Map<String, String> parameters, final User user, final InputStream payload) {
        final ActionParameters params = new ActionParameters();
        // request params
        HttpServletRequest req = mockHttpServletRequest("GET", parameters, null, -1, payload);
        if(!response.toString().isEmpty()) {
            fail("Creating new ActionParams, but response already has content: " + response.toString());
        }
        // response handler
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter output = new PrintWriter(response);
        try {
            Mockito.lenient().when(resp.getWriter()).thenReturn(output);
        }
        catch (IOException ignored ) {}


        params.setRequest(req);
        params.setResponse(resp);
        params.setUser(user);
        params.setLocale(Locale.ENGLISH);

        return params;
    }

    public HttpServletRequest mockHttpServletRequest() {
        return mockHttpServletRequest("GET");
    }

    public HttpServletRequest mockHttpServletRequest(String method) {
        return mockHttpServletRequest(method, null);
    }

    public HttpServletRequest mockHttpServletRequest(String method, Map<String, String> parameters) {
        return mockHttpServletRequest(method, parameters, null, -1, null);
    }

    public HttpServletRequest mockHttpServletRequest(String method,
            Map<String, String> parameters, String contentType,
            int contentLength, InputStream payload) {
        HttpServletRequest req = mock(HttpServletRequest.class);

        if (parameters != null) {
            Mockito.lenient().doReturn(new Vector<>(parameters.keySet()).elements()).when(req).getParameterNames();
            for (String key : parameters.keySet()) {
                Mockito.lenient().when(req.getParameter(key)).thenReturn(parameters.get(key));
            }
        }

        HttpSession session = mock(HttpSession.class);
        Mockito.lenient().doReturn("testkey").when(session).getId();
        Mockito.lenient().doReturn(session).when(req).getSession();

        if (method != null) {
            Mockito.lenient().doReturn(method).when(req).getMethod();
        }

        if (contentType != null) {
            doReturn(contentType).when(req).getContentType();
        }

        if (contentLength >= 0) {
            doReturn(contentLength).when(req).getContentLength();
            doReturn((long) contentLength).when(req).getContentLengthLong();
        }

        if (payload != null) {
            try {
                doReturn(new MockServletInputStream(payload)).when(req).getInputStream();
            } catch (IOException ignore) {}
        }

        return req;
    }

    public HttpServletResponse mockHttpServletResponse() {
        return mockHttpServletResponse(null);
    }

    public HttpServletResponse mockHttpServletResponse(ByteArrayOutputStream baos) {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        if (baos != null) {
            try {
                doReturn(new MockServletOutputStream(baos)).when(resp).getOutputStream();
            } catch (IOException ignore) {}
        }
        return resp;
    }

    public void verifyResponseNotWritten(final ActionParameters params) {
        try {
            verify(params.getResponse(), never()).getWriter();
        } catch (MockitoAssertionError e) {
            // catch and throw to make a more meaningful fail message
            throw new MockitoAssertionError("Was expecting response was not written, but it was!");
        }
        catch (IOException ignore) {}
    }

    public void verifyResponseContent(final JSONObject expectedResult) {
        final JSONObject actualResponse = getResponseJSON();
        JSONTestHelper.shouldEqual(actualResponse, expectedResult);
    }

    public void verifyResponseContent(final JSONArray expectedResult) {
        final JSONArray actualResponse = getResponseJSONArray();
        JSONTestHelper.shouldEqual(actualResponse, expectedResult);
    }

    public void verifyResponseContent(final String expectedResult) {
        final String actualResult = getResponseString();
        assertEquals("Response should match expected", expectedResult, actualResult);
    }

    public void verifyResponseWritten(final ActionParameters params) {
        try {
            verify(params.getResponse(), times(1)).getWriter();
        } catch (MockitoAssertionError e) {
            // catch and throw to make a more meaningful fail message
            throw new MockitoAssertionError("Was expecting response to be written, but it wasn't!");
        }
        catch (IOException ignore) {}
    }

    /**
     * Returns the JSONObject that the route has written in the response
     * @return
     */
    public JSONObject getResponseJSON() {
        return JSONHelper.createJSONObject(getResponseString());
    }

    /**
     * Returns the JSONObject that the route has written in the response
     * @return
     */
    public JSONArray getResponseJSONArray() {
        return JSONHelper.createJSONArray(getResponseString());
    }

    /**
     * Returns the text that the route has written in the response
     * @return
     */
    public String getResponseString() {
        return response.toString();
    }

    public User getGuestUser() {
        return new GuestUser();
    }

    public User getLoggedInUser() {
        User loggedInUser = new User();
        loggedInUser.setId(123);
        loggedInUser.setEmail("test@oskari.org");
        loggedInUser.setFirstname("Test");
        loggedInUser.setLastname("Oskari");
        loggedInUser.setScreenname("Ozkari");
        loggedInUser.setUuid("my uuid is secrets");
        loggedInUser.addRole(1, "User");
        return loggedInUser;
    }

    public User getNotAdminUser() {
        User user = mock(User.class);
        doReturn(false).when(user).isGuest();
        doReturn(false).when(user).isAdmin();
        return user;
    }

    public User getAdminUser() {
        User adminUser = mock(User.class);
        // mock as logged in and admin
        doReturn(false).when(adminUser).isGuest();
        doReturn(true).when(adminUser).isAdmin();
        return adminUser;
    }

    public MapBuilder buildParams() {
        return MapBuilder.build();
    }
}

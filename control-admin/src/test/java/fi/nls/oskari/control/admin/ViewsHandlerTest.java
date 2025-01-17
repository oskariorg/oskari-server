package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceMemory;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceMemory;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.test.control.JSONActionRouteTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ViewsHandlerTest extends JSONActionRouteTest {

    private BundleService bundleService;
    private ViewService viewService;
    private ViewsHandler views;

    @BeforeEach
    public void init() throws ViewException {
        bundleService = new BundleServiceMemory();
        viewService = new ViewServiceMemory();
        views = new ViewsHandler(bundleService, viewService);
    }

    @Test
    public void whenUserIsGuestThrowsException() {
        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest());
        params.setResponse(mockHttpServletResponse());
        params.setUser(getGuestUser());

        try {
            views.handleAction(params);
            Assertions.fail("ActionDeniedException should have been thrown");
        } catch (ActionException e) {
            Assertions.assertEquals("Session expired", e.getMessage());
        }
    }

    @Test
    public void whenUserIsNotAdminThrowsActionException() {
        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest());
        params.setResponse(mockHttpServletResponse());
        params.setUser(getNotAdminUser());

        try {
            views.handleAction(params);
            Assertions.fail("ActionDeniedException should have been thrown");
        } catch (ActionException e) {
            Assertions.assertEquals("Admin only", e.getMessage());
        }
    }

    @Test
    public void whenUuidIsMissingThrowsActionException() {
        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest());
        params.setResponse(mockHttpServletResponse());
        params.setUser(getAdminUser());

        try {
            views.handleAction(params);
            Assertions.fail("ActionException should have been thrown");
        } catch (ActionException e) {
            Assertions.assertEquals("Required parameter 'uuid' missing!", e.getMessage());
        }
    }

    @Test
    public void whenGETtingViewThatDoesNotExistThrowsActionException() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("uuid", "my-unknown-fake-uuid");

        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest("GET", queryParams));
        params.setResponse(mockHttpServletResponse());
        params.setUser(getAdminUser());

        try {
            views.handleAction(params);
            Assertions.fail("ActionException should have been thrown");
        } catch (ActionException e) {
            Assertions.assertEquals("View not found!", e.getMessage());
        }
    }

    @Test
    public void whenGETtingViewThatDoesExistRespondsWithValidJSON()
            throws ActionException, IllegalArgumentException, JSONException, ViewException {
        // Add View to ViewService
        View foo = new View();
        foo.setName("bazqux");
        foo.setType("RANDOM");
        foo.setCreated(OffsetDateTime.parse("2022-05-18T12:15:15.15Z", DateTimeFormatter.ISO_DATE_TIME));
        foo.setUpdated(OffsetDateTime.parse("2022-05-18T12:15:15.15Z", DateTimeFormatter.ISO_DATE_TIME));
        viewService.addView(foo);

        // Query for the View
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("uuid", foo.getUuid());

        ByteArrayOutputStream respOut = new ByteArrayOutputStream();

        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest("GET", queryParams));
        params.setResponse(mockHttpServletResponse(respOut));
        params.setUser(getAdminUser());

        views.handleAction(params);

        byte[] body = respOut.toByteArray();
        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.length > 0);
        String jsonStr = new String(body, StandardCharsets.UTF_8);
        JSONObject viewJSON = new JSONObject(jsonStr);
        View view = ViewHelper.viewFromJson(bundleService, viewJSON);

        // id and uuid should not be copied over with export functionality
        // default id is -1, default uuid is null
        Assertions.assertEquals(-1L, view.getId());
        Assertions.assertNull(view.getUuid());

        // name and type should be same as what we added to the service
        Assertions.assertEquals(foo.getName(), view.getName());
        Assertions.assertEquals(foo.getType(), view.getType());

        respOut.toByteArray();
    }

    @Test
    public void whenPOSTingValidJSONRespondsWithJSONContainingTheIdAndUuid()
            throws JSONException, ActionException {
        View view = getDummyView();
        JSONObject viewJSON = ViewHelper.viewToJson(bundleService, view);
        byte[] rawInput = viewJSON.toString().getBytes(StandardCharsets.UTF_8);
        InputStream payload = new ByteArrayInputStream(rawInput);

        ByteArrayOutputStream respOut = new ByteArrayOutputStream();

        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest("POST", null,
                "application/json;charset=UTF-8", rawInput.length, payload));
        params.setResponse(mockHttpServletResponse(respOut));
        params.setUser(getAdminUser());

        views.handleAction(params);

        byte[] body = respOut.toByteArray();
        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.length > 0);

        String bodyStr = new String(body, StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(bodyStr);

        Assertions.assertTrue(json.has("id"));
        Assertions.assertTrue(json.getLong("id") > -1L);
        Assertions.assertTrue(json.has("uuid"));
        String uuid = json.getString("uuid");
        Assertions.assertNotNull(uuid);
        Assertions.assertEquals(36, uuid.length());
    }

    private static View getDummyView() {
        View view = new View();
        view.setName("My Default View");
        view.setType("DEFAULT");
        view.setIsDefault(true);
        view.setIsPublic(true);
        view.setOnlyForUuId(false);
        view.setApplication("foo");
        view.setPage("bar");
        view.setCreated(OffsetDateTime.parse("2022-05-18T12:15:15.15Z", DateTimeFormatter.ISO_DATE_TIME));
        view.setUpdated(OffsetDateTime.parse("2022-05-18T12:15:15.15Z", DateTimeFormatter.ISO_DATE_TIME));

        return view;
    }

}

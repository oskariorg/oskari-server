package fi.nls.oskari.control.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.test.control.JSONActionRouteTest;

public class ViewsHandlerTest extends JSONActionRouteTest {

    private static BundleService bundleService;
    private static ViewService viewService;
    private static ViewsHandler views;
    private static Bundle foobar;
    private static View bazqux;

    @BeforeClass
    public static void init() throws ViewException {
        bundleService = new BundleServiceMemory();
        viewService = new ViewServiceMemory();

        foobar = new Bundle();
        foobar.setName("foobar");
        bundleService.addBundleTemplate(foobar);
        assertEquals(1, bundleService.findAll().size());
        assertEquals(0L, foobar.getBundleId());

        bazqux = new View();
        bazqux.setName("bazqux");
        bazqux.setType("RANDOM");
        viewService.addView(bazqux);
        assertEquals(1, viewService.findAll().size());
        assertEquals(0L, bazqux.getId());
        assertNotNull(bazqux.getUuid());
        assertEquals(36, bazqux.getUuid().length());

        views = new ViewsHandler(bundleService, viewService);
    }

    @AfterClass
    public static void teardown() {
        bundleService = null;
        viewService = null;
        views = null;
        foobar = null;
        bazqux = null;
    }

    @Test
    public void testThatParsingWorks() throws IOException, IllegalArgumentException, JSONException {
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("view-to-import.json")) {
            byte[] b = IOHelper.readBytes(in);
            in.close();
            String jsonStr = new String(b, StandardCharsets.UTF_8);
            JSONObject viewJSON = new JSONObject(jsonStr);

            View view = ViewHelper.viewFromJson(bundleService, viewJSON);
            assertEquals("Default view", view.getName());
            assertEquals("DEFAULT", view.getType());
            assertEquals(true, view.isDefault());
            assertEquals(true, view.isPublic());
            assertEquals(false, view.isOnlyForUuId());
            assertEquals("servlet", view.getApplication());
            assertEquals("index", view.getPage());
            assertEquals("/applications/sample", view.getDevelopmentPath());

            List<Bundle> bundles = view.getBundles();
            assertNotNull(bundles);
            assertEquals(1, bundles.size());
            assertEquals("foobar", bundles.get(0).getName());
        }
    }

    @Test
    public void whenExportedAndImportedDataRemainsTheSame() throws JSONException {
        View view1 = getDummyView();
        JSONObject viewJSON = ViewHelper.viewToJson(bundleService, view1);
        View view2 = ViewHelper.viewFromJson(bundleService, viewJSON);

        assertEquals(view1.getName(), view2.getName());
        assertEquals(view1.getType(), view2.getType());
        assertEquals(view1.isDefault(), view2.isDefault());
        assertEquals(view1.isPublic(), view2.isPublic());
        assertEquals(view1.isOnlyForUuId(), view2.isOnlyForUuId());
        assertEquals(view1.getApplication(), view2.getApplication());
        assertEquals(view1.getPage(), view2.getPage());
        assertEquals(view1.getDevelopmentPath(), view2.getDevelopmentPath());
        assertEquals(view1.getBundles(), view2.getBundles());
    }

    @Test
    public void testGuestUsersShallNotPass() {
        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest());
        params.setResponse(mockHttpServletResponse());
        params.setUser(getGuestUser());

        try {
            views.handleAction(params);
            fail("ActionDeniedException should have been thrown");
        } catch (ActionException e) {
            assertEquals("Admin only", e.getMessage());
        }
    }

    @Test
    public void testNonAdminUserShallNotPass() {
        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest());
        params.setResponse(mockHttpServletResponse());
        params.setUser(getNotAdminUser());

        try {
            views.handleAction(params);
            fail("ActionDeniedException should have been thrown");
        } catch (ActionException e) {
            assertEquals("Admin only", e.getMessage());
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
            fail("ActionException should have been thrown");
        } catch (ActionException e) {
            assertEquals("Required parameter 'uuid' missing!", e.getMessage());
        }
    }

    @Test
    public void whenNoSuchViewExistsThrowsActionException() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("uuid", "my-unknown-fake-uuid");

        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest("GET", queryParams));
        params.setResponse(mockHttpServletResponse());
        params.setUser(getAdminUser());

        try {
            views.handleAction(params);
            fail("ActionException should have been thrown");
        } catch (ActionException e) {
            assertEquals("View not found!", e.getMessage());
        }
    }

    @Test
    public void testGettingWorks() throws ActionException, IllegalArgumentException, JSONException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("uuid", bazqux.getUuid());

        ByteArrayOutputStream respOut = new ByteArrayOutputStream();

        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest("GET", queryParams));
        params.setResponse(mockHttpServletResponse(respOut));
        params.setUser(getAdminUser());

        views.handleAction(params);

        byte[] body = respOut.toByteArray();
        assertNotNull(body);
        assertTrue(body.length > 0);
        String jsonStr = new String(body, StandardCharsets.UTF_8);
        JSONObject viewJSON = new JSONObject(jsonStr);
        View view = ViewHelper.viewFromJson(bundleService, viewJSON);
        // id and uuid should not be copied over with export functionality
        // default id is -1
        assertEquals(-1L, view.getId());
        assertNull(view.getUuid());
        assertEquals(bazqux.getName(), view.getName());
        assertEquals(bazqux.getType(), view.getType());

        respOut.toByteArray();
    }

    @Test
    public void whenPOSTingValidJsonRespondsWithJsonContainingTheIdAndUuid() throws JSONException, ActionException {
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
        assertNotNull(body);
        assertTrue(body.length > 0);

        String bodyStr = new String(body, StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(bodyStr);

        assertTrue(json.has("id"));
        assertTrue(json.has("uuid"));
        String uuid = json.getString("uuid");
        assertNotNull(uuid);
        assertEquals(36, uuid.length());
    }

    private View getDummyView() {
        View view = new View();
        view.setName("My Default View");
        view.setType("DEFAULT");
        view.setIsDefault(true);
        view.setIsPublic(true);
        view.setOnlyForUuId(false);
        view.setApplication("foo");
        view.setPage("bar");
        view.setDevelopmentPath("baz");
        view.setBundles(Arrays.asList(new Bundle[] { foobar }));
        return view;
    }

}

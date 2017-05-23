package fi.nls.oskari.control.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.test.control.JSONActionRouteTest;

public class ViewsHandlerTest extends JSONActionRouteTest {
    
    private static ViewsHandler views;
    private static Bundle foobar;
    
    @BeforeClass
    public static void init() {
        BundleService bundleService = new BundleServiceMemory();
        
        foobar = new Bundle();
        foobar.setName("foobar");
        bundleService.addBundleTemplate(foobar);
        
        ViewService viewService = new ViewServiceMemory();
        views = new ViewsHandler(bundleService, viewService);
    }
    
    @Test
    public void testGuestUsersShallNotPass() {
        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest("GET", null, null));
        params.setResponse(mockHttpServletResponse(null));
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
        params.setRequest(mockHttpServletRequest("GET", null, null));
        params.setResponse(mockHttpServletResponse(null));
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
        params.setRequest(mockHttpServletRequest("GET", null, null));
        params.setResponse(mockHttpServletResponse(null));
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
        queryParams.put("uuid", "my-own-fake-uuid");
        
        ActionParameters params = new ActionParameters();
        params.setRequest(mockHttpServletRequest("GET", queryParams, null));
        params.setResponse(mockHttpServletResponse(null));
        params.setUser(getAdminUser());
        
        try {
            views.handleAction(params);
            fail("ActionException should have been thrown");
        } catch (ActionException e) {
            assertEquals("View not found!", e.getMessage());
        }
    }
    
    @Test
    public void testThatParsingWorks() throws IOException, IllegalArgumentException, JSONException {
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("view-to-import.json")) {
            byte[] b = IOHelper.readBytes(in);
            in.close();
            
            View view = views.viewFromJson(b);
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
        View view1 = new View();
        view1.setId(1L);
        view1.setName("My Default View");
        view1.setType("DEFAULT");
        view1.setIsDefault(true);
        view1.setIsPublic(true);
        view1.setOnlyForUuId(false);
        view1.setApplication("foo");
        view1.setPage("bar");
        view1.setDevelopmentPath("baz");
        view1.setBundles(Arrays.asList(new Bundle[] { foobar }));
        
        View view2 = views.viewFromJson(views.viewToJson(view1));
        
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
    
}

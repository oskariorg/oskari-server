package fi.nls.oskari.map.view.util;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceMemory;
import fi.nls.oskari.util.IOHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class ViewHelperTest {

    private BundleService bundleService;

    @Before
    public void init() {
        bundleService = new BundleServiceMemory();
    }

    @Test
    public void testThatParsingWorks() 
            throws IOException, IllegalArgumentException, JSONException {
        byte[] b = null;
        try (InputStream in = this.getClass()
                .getResourceAsStream("view-to-import.json")) {
            b = IOHelper.readBytes(in);
        }
        if (b == null || b.length == 0) {
            fail("Failed to read view-to-import.json");
            return;
        }

        String jsonStr = new String(b, StandardCharsets.UTF_8);
        JSONObject viewJSON = new JSONObject(jsonStr);

        // The values should match the ones in view-to-import.json
        // We must register "foobar" Bundle to the BundleService beforehand
        Bundle foobar = new Bundle();
        foobar.setName("foobar");
        bundleService.addBundleTemplate(foobar);

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
        assertEquals(foobar.getName(), bundles.get(0).getName());
    }

    @Test
    public void whenConvertedToJSONAndBackValuesRemainTheSame() throws JSONException {
        // Register random bundle
        Bundle randomBundle = new Bundle();
        randomBundle.setName(UUID.randomUUID().toString());
        bundleService.addBundleTemplate(randomBundle);

        View view1 = new View();
        view1.setName("My View");
        view1.setType("DEFAULT");
        view1.setIsDefault(true);
        view1.setIsPublic(true);
        view1.setOnlyForUuId(false);
        view1.setApplication("foo");
        view1.setPage("bar");
        view1.setDevelopmentPath("baz");
        view1.addBundle(randomBundle);

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

        List<Bundle> bundles1 = view1.getBundles();
        List<Bundle> bundles2 = view2.getBundles();
        assertNotNull(bundles1);
        assertNotNull(bundles2);
        assertEquals(bundles1.size(), bundles2.size());
        for (int i = 0; i < bundles1.size(); i++) {
            Bundle b1 = bundles1.get(i);
            Bundle b2 = bundles2.get(i);
            assertEquals(b1.getName(), b2.getName());
        }
    }

}

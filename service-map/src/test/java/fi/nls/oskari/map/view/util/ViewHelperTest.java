package fi.nls.oskari.map.view.util;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceMemory;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ViewHelperTest {

    private BundleService bundleService;

    @BeforeEach
    public void init() {
        bundleService = new BundleServiceMemory();
    }

    @Test
    public void testReferer() throws Exception {
        PropertyUtil.addProperty("oskari.domain", "https://testdomain.org");
        PropertyUtil.addProperty("view.published.usage.unrestrictedDomains", "legit.com, dummy.org");
        // work around for static helper that uses property values that are set before this test and might result this test to fail
        ViewHelper.setInstanceAddress(null);
        ViewHelper.setUnrestrictedUsageDomains(PropertyUtil.getCommaSeparatedList("view.published.usage.unrestrictedDomains"));
        Assertions.assertTrue(ViewHelper.isRefererDomain(null, "http://testing.net"), "Null-referer should be ok");
        Assertions.assertTrue(ViewHelper.isRefererDomain("https://testdomain.org", "http://testing.net"), "Instance domain as referer should be ok");
        Assertions.assertTrue(ViewHelper.isRefererDomain("https://legit.com", "http://testing.net"), "Unrestricted domain 1 as referer should be ok");
        Assertions.assertTrue(ViewHelper.isRefererDomain("https://dummy.org", "http://testing.net"), "Unrestricted domain 2 as referer should be ok");

        Assertions.assertFalse(ViewHelper.isRefererDomain("https://yay.com", "http://testing.net"), "Random referer should NOT be ok");
        PropertyUtil.clearProperties();
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
            Assertions.fail("Failed to read view-to-import.json");
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
        Assertions.assertEquals("Default view", view.getName());
        Assertions.assertEquals("DEFAULT", view.getType());
        Assertions.assertEquals(true, view.isDefault());
        Assertions.assertEquals(true, view.isPublic());
        Assertions.assertEquals(false, view.isOnlyForUuId());
        Assertions.assertEquals("servlet", view.getApplication());
        Assertions.assertEquals("index", view.getPage());

        List<Bundle> bundles = view.getBundles();
        Assertions.assertNotNull(bundles);
        Assertions.assertEquals(1, bundles.size());
        Assertions.assertEquals(foobar.getName(), bundles.get(0).getName());
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
        view1.setCreated(OffsetDateTime.parse("2022-05-18T12:00:00.00Z", DateTimeFormatter.ISO_DATE_TIME));
        view1.setUpdated(OffsetDateTime.parse("2022-05-18T12:00:00.00Z", DateTimeFormatter.ISO_DATE_TIME));
        view1.addBundle(randomBundle);

        JSONObject viewJSON = ViewHelper.viewToJson(bundleService, view1);
        View view2 = ViewHelper.viewFromJson(bundleService, viewJSON);

        Assertions.assertEquals(view1.getName(), view2.getName());
        Assertions.assertEquals(view1.getType(), view2.getType());
        Assertions.assertEquals(view1.isDefault(), view2.isDefault());
        Assertions.assertEquals(view1.isPublic(), view2.isPublic());
        Assertions.assertEquals(view1.isOnlyForUuId(), view2.isOnlyForUuId());
        Assertions.assertEquals(view1.getApplication(), view2.getApplication());
        Assertions.assertEquals(view1.getPage(), view2.getPage());

        List<Bundle> bundles1 = view1.getBundles();
        List<Bundle> bundles2 = view2.getBundles();
        Assertions.assertNotNull(bundles1);
        Assertions.assertNotNull(bundles2);
        Assertions.assertEquals(bundles1.size(), bundles2.size());
        for (int i = 0; i < bundles1.size(); i++) {
            Bundle b1 = bundles1.get(i);
            Bundle b2 = bundles2.get(i);
            Assertions.assertEquals(b1.getName(), b2.getName());
        }
    }

}

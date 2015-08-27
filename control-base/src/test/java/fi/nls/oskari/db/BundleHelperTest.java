package fi.nls.oskari.db;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class BundleHelperTest {

    private static final String STARTUP_TEST =
            "{\n" +
                    "    \"title\": \"Title\",\n" +
                    "    \"bundleinstancename\": \"mybundle\",\n" +
                    "    \"bundlename\": \"mybundle\",\n" +
                    "    \"metadata\": {\n" +
                    "        \"Import-Bundle\": {\n" +
                    "            \"mybundle\": {\n" +
                    "                \"bundlePath\": \"/Oskari/packages/mynamespace/bundle/\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

    @Test
    public void testGetDefaultBundleStartup()
            throws Exception {
        assertEquals(STARTUP_TEST, BundleHelper.getDefaultBundleStartup("mynamespace", "mybundle", "Title"));
    }
}
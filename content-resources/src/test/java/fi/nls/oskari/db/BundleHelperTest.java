package fi.nls.oskari.db;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class BundleHelperTest {

    private static final String STARTUP_TEST_FILE = "BundleHelperTest-startup.json";

    @Test
    public void testGetDefaultBundleStartup()
            throws Exception {
        final String startupJson = IOHelper.readString(getClass().getResourceAsStream(STARTUP_TEST_FILE));
        JSONObject expected = JSONHelper.createJSONObject(startupJson);
        JSONObject actual = JSONHelper.createJSONObject(BundleHelper.getDefaultBundleStartup("mynamespace", "mybundle", "Title"));
        assertTrue(JSONHelper.isEqual(expected, actual));
    }
}
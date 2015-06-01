package fi.nls.test.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.junit.Assert.assertTrue;

/**
 * Helper for JUnit tests. Logs the JSONs if test fails.
 */
public class JSONTestHelper {

    private static final Logger LOGGER = LogFactory.getLogger(JSONTestHelper.class);

    private JSONTestHelper() {}

    public static void shouldEqual(final JSONObject actualResponse, final JSONObject expectedResult) {
        boolean success = false;
        try {

            assertTrue("Response should match expected", JSONHelper.isEqual(expectedResult, actualResponse));
            success = true;
        } finally {
            if(!success) {
                try {
                    printError(expectedResult.toString(2), actualResponse.toString(2));
                } catch (JSONException ignored) {
                    LOGGER.error("Couldn't print out the jsons");
                    LOGGER.ignore(ignored);
                }
            }
        }
    }

    public static void shouldEqual(final JSONArray actualResponse, final JSONArray expectedResult) {
        boolean success = false;
        try {

            assertTrue("Response should match expected", JSONHelper.isEqual(expectedResult, actualResponse));
            success = true;
        } finally {
            if(!success) {
                try {
                    printError(expectedResult.toString(2), actualResponse.toString(2));
                } catch (JSONException ignored) {
                    LOGGER.error("Couldn't print out the jsons");
                    LOGGER.ignore(ignored);
                }
            }
        }
    }

    private static void printError(final String expected, final String actual) {
        LOGGER.error(">>>>>> Expected:\n" + expected);
        LOGGER.error("  =======  Actual:");
        LOGGER.error(actual);
        LOGGER.error("<<<<<<<<<<<");
    }
}

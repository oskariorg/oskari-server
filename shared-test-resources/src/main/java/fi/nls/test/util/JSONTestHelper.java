package fi.nls.test.util;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.junit.Assert.assertTrue;

/**
 * Helper for JUnit tests. Logs the JSONs if test fails.
 */
public class JSONTestHelper {

    public static void shouldEqual(final JSONObject actualResponse, final JSONObject expectedResult) {
        boolean success = false;
        try {

            assertTrue("Response should match expected", JSONHelper.isEqual(expectedResult, actualResponse));
            success = true;
        }
        finally {
            if(!success) {
                try {
                    System.out.println(">>>>>> Expected:\n" + expectedResult.toString(2));
                    System.out.println("  =======  Actual:");
                    System.out.println(actualResponse.toString(2));
                    System.out.println("<<<<<<<<<<<");
                } catch (JSONException ignored) {
                    System.out.println("Couldn't print out the jsons");
                }
            }
        }
    }

    public static void shouldEqual(final JSONArray actualResponse, final JSONArray expectedResult) {
        boolean success = false;
        try {

            assertTrue("Response should match expected", JSONHelper.isEqual(expectedResult, actualResponse));
            success = true;
        }
        finally {
            if(!success) {
                try {
                    System.out.println(">>>>>> Expected:\n" + expectedResult.toString(2));
                    System.out.println("  =======  Actual:");
                    System.out.println(actualResponse.toString(2));
                    System.out.println("<<<<<<<<<<<");
                } catch (JSONException ignored) {
                    System.out.println("Couldn't print out the jsons");
                }
            }
        }
    }
}

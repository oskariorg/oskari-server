package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.TestHelper;
import org.junit.Ignore;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.powermock.utils.Asserts;

public class UnsdRequestTest {

    private static final int EXPECTED_INDICATOR_RESPONSE_DATA_SIZE = 250;
    private static final String INDICATOR = "1.5.2";

    private UnsdRequest request;

    public UnsdRequestTest() {
        request = new UnsdRequest(new UnsdConfig());
        request.setGoal("1");
    }

    @Test
    @Ignore("Requires external HTTP requests to be made")
    public void testRequestTargets() throws JSONException {

        org.junit.Assume.assumeTrue(TestHelper.canDoHttp());
        org.junit.Assume.assumeTrue(TestHelper.redisAvailable());

        String json = request.getTargets();
        Asserts.assertNotNull(json, "Targets response is null.");
        assertNotEquals("Targets response is empty.", "", json);

        JSONObject goal = getFirstObject(json);
        assertTrue("Goal has no targets.", goal.has("targets"));

        JSONArray targets = goal.getJSONArray("targets");
        for (int i = 0; i < targets.length(); i++) {
            JSONObject target = targets.getJSONObject(i);
            assertTrue("Target has no indicators", target.has("indicators"));
        }
    }

    public JSONObject getFirstObject(String json) throws JSONException {
        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (JSONException ex) {
            obj = JSONHelper.createJSONArray(json).getJSONObject(0);
        }
        return obj;
    }

    @Test
    @Ignore("Requires external HTTP requests to be made")
    public void testRequestDimensions() throws JSONException {
        org.junit.Assume.assumeTrue(TestHelper.canDoHttp());
        org.junit.Assume.assumeTrue(TestHelper.redisAvailable());

        String json = request.getDimensions();
        JSONArray dimensions = JSONHelper.createJSONArray(json);
        for (int i = 0; i < dimensions.length(); i++) {
            JSONObject dimension = dimensions.getJSONObject(i);
            assertNotNull("dimension has no id", dimension.optString("id", null));
            assertNotNull("dimension has no codes", dimension.optJSONArray("codes"));
        }
    }

    @Test
    @Ignore("Requires external HTTP requests to be made")
    public void testIndicatorDataResponseDataSize() throws JSONException {
        org.junit.Assume.assumeTrue(TestHelper.canDoHttp());
        org.junit.Assume.assumeTrue(TestHelper.redisAvailable());

        request.setIndicator(INDICATOR);
        String response = request.getIndicatorData(null);
        JSONObject indicatorData = getFirstObject(response);
        int sizeAttribute = (int) indicatorData.get("size");
        JSONArray data = (JSONArray) indicatorData.get("data");
        assertEquals(EXPECTED_INDICATOR_RESPONSE_DATA_SIZE, sizeAttribute);
        assertEquals(EXPECTED_INDICATOR_RESPONSE_DATA_SIZE, data.length());
    }
}

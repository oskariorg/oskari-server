package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.TestHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class UnsdRequestTest {

    private static final int EXPECTED_INDICATOR_RESPONSE_DATA_SIZE = 2500;
    private static final String INDICATOR = "1.5.2";

    private UnsdRequest request;

    public UnsdRequestTest() {
        request = new UnsdRequest(new UnsdConfig());
        request.setGoal("1");
    }

    @Test
    @Disabled("Requires external HTTP requests to be made")
    public void testRequestTargets() throws JSONException {

        Assumptions.assumeTrue(TestHelper.canDoHttp());
        Assumptions.assumeTrue(TestHelper.redisAvailable());

        String json = request.getTargets();
        Assertions.assertNotNull(json, "Targets response is null.");
        Assertions.assertNotEquals("", json, "Targets response is empty.");

        JSONObject goal = getFirstObject(json);
        Assertions.assertTrue(goal.has("targets"), "Goal has no targets.");

        JSONArray targets = goal.getJSONArray("targets");
        for (int i = 0; i < targets.length(); i++) {
            JSONObject target = targets.getJSONObject(i);
            Assertions.assertTrue(target.has("indicators"), "Target has no indicators");
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
    @Disabled("Requires external HTTP requests to be made")
    public void testRequestDimensions() throws JSONException {
        Assumptions.assumeTrue(TestHelper.canDoHttp());
        Assumptions.assumeTrue(TestHelper.redisAvailable());

        String json = request.getDimensions();
        JSONArray dimensions = JSONHelper.createJSONArray(json);
        for (int i = 0; i < dimensions.length(); i++) {
            JSONObject dimension = dimensions.getJSONObject(i);
            Assertions.assertNotNull(dimension.optString("id", null), "dimension has no id");
            Assertions.assertNotNull(dimension.optJSONArray("codes"), "dimension has no codes");
        }
    }

    @Test
    @Disabled("Requires external HTTP requests to be made")
    public void testIndicatorDataResponseDataSize() throws JSONException {
        Assumptions.assumeTrue(TestHelper.canDoHttp());
        Assumptions.assumeTrue(TestHelper.redisAvailable());

        request.setIndicator(INDICATOR);
        String response = request.getIndicatorData(null);
        JSONObject indicatorData = getFirstObject(response);
        int sizeAttribute = (int) indicatorData.get("size");
        JSONArray data = (JSONArray) indicatorData.get("data");
        Assertions.assertEquals(EXPECTED_INDICATOR_RESPONSE_DATA_SIZE, sizeAttribute);
        Assertions.assertEquals(EXPECTED_INDICATOR_RESPONSE_DATA_SIZE, data.length());
    }
}

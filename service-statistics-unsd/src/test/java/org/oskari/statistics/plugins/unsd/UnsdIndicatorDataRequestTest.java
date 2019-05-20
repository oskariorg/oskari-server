package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.TestHelper;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.powermock.utils.Asserts;

@RunWith(PowerMockRunner.class)
public class UnsdIndicatorDataRequestTest {

    private UnsdRequest request;

    public UnsdIndicatorDataRequestTest() {
        request = new UnsdRequest(new UnsdConfig());
        request.setGoal("1");
    }

    @Test
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

    public JSONObject getFirstObject (String json) throws JSONException {
        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (JSONException ex) {
            obj = JSONHelper.createJSONArray(json).getJSONObject(0);
        }
        return obj;
    }

    @Test
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
}

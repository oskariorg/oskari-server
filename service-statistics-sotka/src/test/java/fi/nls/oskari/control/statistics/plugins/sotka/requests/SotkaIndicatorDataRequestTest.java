package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.statistics.plugins.sotka.requests.IndicatorData;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.test.util.ResourceHelper;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

@RunWith(PowerMockRunner.class)
public class SotkaIndicatorDataRequestTest {
    private static String testResponse = ResourceHelper.readStringResource("SotkaIndicatorData.csv",
            SotkaIndicatorDataRequestTest.class);
    private static String expectedParsingResult = ResourceHelper.readStringResource("SotkaIndicatorData.json",
            SotkaIndicatorDataRequestTest.class);

    @Test
    public void testParseIndicators() throws JSONException {
        SotkaRequest request = SotkaRequest.getInstance(IndicatorData.NAME);
        String json = request.getJsonFromCSV(testResponse);
        JSONArray resultArray = new JSONArray(json);
        JSONArray expectedArray = new JSONArray(expectedParsingResult);
        assertEquals(466, resultArray.length());
        for (int i = 0; i < 466; i++) {
            JSONObject resultObject = resultArray.getJSONObject(i);
            JSONObject expectedObject = expectedArray.getJSONObject(i);
            for (String name : JSONObject.getNames(expectedObject)) {
                assertEquals(expectedObject.getString(name), resultObject.getString(name));
            }
        }
    }
}

package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import fi.nls.test.util.ResourceHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
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

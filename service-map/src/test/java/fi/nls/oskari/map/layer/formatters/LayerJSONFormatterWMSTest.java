package fi.nls.oskari.map.layer.formatters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LayerJSONFormatterWMSTest {

    private JSONObject generateCaps(String... values)  throws JSONException{
        JSONObject caps = new JSONObject();
        if (values != null) {
            JSONArray times = new JSONArray();
            for(String value: values) {
                times.put(value);
            }
            caps.put("times", times);
        }
        return caps;
    }

    @Test
    public void getTimesFromCapabilitiesNone() throws JSONException {
        JSONObject caps = generateCaps();
        JSONArray parsed = LayerJSONFormatterWMS.getTimesFromCapabilities(caps);
        Assertions.assertNull(parsed, "No times should be parsed as 'no timeseries'");
    }
    @Test
    public void getTimesFromCapabilitiesSingle() throws JSONException {
        JSONObject caps = generateCaps("2021");
        JSONArray parsed = LayerJSONFormatterWMS.getTimesFromCapabilities(caps);
        Assertions.assertNull(parsed, "Single year should be parsed as 'no timeseries'");
    }
    @Test
    public void getTimesFromCapabilitiesMultiple() throws JSONException {
        JSONObject caps = generateCaps("2021", "2022");
        JSONArray parsed = LayerJSONFormatterWMS.getTimesFromCapabilities(caps);
        Assertions.assertEquals(2, parsed.length(), "Two years should be parsed as having timeseries");
    }
    @Test
    public void getTimesFromCapabilitiesRangeWithoutResolution() throws JSONException {
        JSONObject caps = generateCaps("1970-01-01/2022-12-1");
        JSONArray parsed = LayerJSONFormatterWMS.getTimesFromCapabilities(caps);
        Assertions.assertNull(parsed, "Only start/end without resolution should be parsed as 'no timeseries'");
    }
    @Test
    public void getTimesFromCapabilitiesRangeWithResolution() throws JSONException {
        JSONObject caps = generateCaps("1970-01-01/2022-12-1/P1Y");
        JSONArray parsed = LayerJSONFormatterWMS.getTimesFromCapabilities(caps);
        Assertions.assertEquals(1, parsed.length(), "Returned as is when there is start/end/resolution");
    }
}
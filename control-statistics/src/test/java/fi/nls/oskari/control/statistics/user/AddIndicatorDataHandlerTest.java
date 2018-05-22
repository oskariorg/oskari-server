package fi.nls.oskari.control.statistics.user;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 18.5.2018.
 */
public class AddIndicatorDataHandlerTest {

    @Test
    public void testParseIndicatorData() throws Exception {
        AddIndicatorDataHandler h = new AddIndicatorDataHandler();
        Map<String, IndicatorValue> values = h.parseIndicatorData("{\"region\": 0.3}");
        assertNotNull("Values shouldn't be null", values);
        assertEquals("Should have one value", 1, values.size());

        JSONObject test = new JSONObject();
        values.get("region").putToJSONObject(test, "testKey");
        assertEquals("Should have one value", "0.3", test.optString("testKey"));

    }
}
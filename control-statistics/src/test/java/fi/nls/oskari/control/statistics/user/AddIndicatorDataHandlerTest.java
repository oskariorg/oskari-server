package fi.nls.oskari.control.statistics.user;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Created by SMAKINEN on 18.5.2018.
 */
public class AddIndicatorDataHandlerTest {

    @Test
    public void testParseIndicatorData() throws Exception {
        AddIndicatorDataHandler h = new AddIndicatorDataHandler();
        Map<String, IndicatorValue> values = h.parseIndicatorData("{\"region\": 0.3}");
        Assertions.assertNotNull(values, "Values shouldn't be null");
        Assertions.assertEquals(1, values.size(), "Should have one value");

        JSONObject test = new JSONObject();
        values.get("region").putToJSONObject(test, "testKey");
        Assertions.assertEquals("0.3", test.optString("testKey"), "Should have one value");

    }
}
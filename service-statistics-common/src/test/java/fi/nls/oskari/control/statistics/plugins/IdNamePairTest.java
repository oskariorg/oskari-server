package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.data.IdNamePair;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by SMAKINEN on 26.9.2016.
 */
public class IdNamePairTest {

    @Test
    public void testSimpleToJSON()
            throws Exception {
        IdNamePair val =  new IdNamePair("id");
        Assertions.assertEquals("id", val.getValueForJson(), "Should only return value");
    }
    @Test
    public void testToJSON()
            throws Exception {
        IdNamePair val =  new IdNamePair("id", "value");
        JSONObject expected = JSONHelper.createJSONObject("{\"id\":\"id\",\"name\":\"value\"}");
        Assertions.assertTrue(JSONHelper.isEqual(expected, (JSONObject) val.getValueForJson()), "Should only return value");

    }
}
package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.data.IdNamePair;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 26.9.2016.
 */
public class IdNamePairTest {

    @Test
    public void testSimpleToJSON()
            throws Exception {
        IdNamePair val =  new IdNamePair("id");
        assertEquals("Should only return value", "id", val.getValueForJson());
    }
    @Test
    public void testToJSON()
            throws Exception {
        IdNamePair val =  new IdNamePair("id", "value");
        JSONObject expected = JSONHelper.createJSONObject("{\"id\":\"id\",\"name\":\"value\"}");
        assertTrue("Should only return value", JSONHelper.isEqual(expected, (JSONObject) val.getValueForJson()));

    }
}
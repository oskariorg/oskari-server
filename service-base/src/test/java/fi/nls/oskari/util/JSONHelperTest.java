package fi.nls.oskari.util;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author SMAKINEN
 */
public class JSONHelperTest {
    @Test
    public void testCreateJSONObjectFromString() {
        JSONObject obj = JSONHelper.createJSONObject("{key: \"value\"}");
        assertTrue("JSONObject was created", obj != null);
        assertEquals("JSONObject should have key 'key' with value 'value'", obj.optString("key"), "value");

        obj = JSONHelper.createJSONObject("{ \"key\": \"value\"}");
        assertTrue("JSONObject was created", obj != null);
        assertEquals("JSONObject should have key 'key' with value 'value'", obj.optString("key"), "value");

        obj = JSONHelper.createJSONObject("{}");
        assertTrue("JSONObject was created", obj != null);
        assertEquals("JSONObject should be empty", obj.length(), 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateJSONObjectFromEmptyString() {

        JSONObject obj = JSONHelper.createJSONObject("");
        assertTrue("JSONObject was not created", obj == null);
        throw new IllegalStateException("Should not get this far");

        // TODO: more error cases
    }

    @Test
    public void testCreateJSONObjectFromKeyValue() throws Exception {
        JSONObject obj = JSONHelper.createJSONObject("key", "value");
        assertTrue("JSONObject was created", obj != null);
        assertEquals("JSONObject should have key 'key' with value 'value'", obj.optString("key"), "value");

        obj = JSONHelper.createJSONObject("key", null);
        assertTrue("JSONObject was created", obj != null);
        assertEquals("JSONObject should have key 'key' with empty value", obj.optString("key"), "");

        obj = JSONHelper.createJSONObject(null, "value");
        assertTrue("JSONObject was created", obj != null);
        assertEquals("JSONObject should be empty", obj.length(), 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetJSONObjectWithNoObject() throws Exception {
        JSONObject obj = JSONHelper.createJSONObject("key", "value");
        assertEquals("JSONObject 'key' should return 'value'", JSONHelper.getJSONObject(obj, "key"), "value");
        throw new IllegalStateException("Should not get this far");
    }

    @Test
    public void testGetJSONObject() throws Exception {
        JSONObject obj = JSONHelper.createJSONObject("key", "value");
        JSONObject innerObj = JSONHelper.createJSONObject("innerkey", "innervalue");
        assertTrue("Inserting inner object should be successful", JSONHelper.putValue(obj, "inner", innerObj));
        assertEquals("JSONObject 'inner' should return innerObj", JSONHelper.getJSONObject(obj, "inner"), innerObj);
    }

    @Test
    public void testGetJSONArray() throws Exception {

    }

    @Test
    public void testGetEmptyIfNull() throws Exception {

    }

    @Test
    public void testGetStringFromJSON() throws Exception {

    }
/*
    @Test
    public void testPutValue() throws Exception {

    }

    @Test
    public void testPutValue() throws Exception {

    }

    @Test
    public void testPutValue() throws Exception {

    }

    @Test
    public void testPutValue() throws Exception {

    }

    @Test
    public void testPutValue() throws Exception {

    }

    @Test
    public void testPutValue() throws Exception {

    }

    @Test
    public void testPutValue() throws Exception {

    }
*/
    @Test
    public void testCreateJSONArray() throws Exception {

    }

    @Test
    public void testIsEqual() throws Exception {

    }
}

package fi.nls.oskari.util;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

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

    @Test
    public void testCreateJSONObjectFromEmptyString() {

        JSONObject obj = JSONHelper.createJSONObject("");
        assertTrue("JSONObject was not created", obj == null);

        obj = JSONHelper.createJSONObject(null);
        assertTrue("JSONObject was not created", obj == null);
        // TODO: more error cases
    }

    @Test
    public void testCreateJSONObjectFromNullTokener() {

        JSONObject obj = JSONHelper.createJSONObject4Tokener(null);
        assertTrue("JSONObject was not created", obj == null);


    }
    @Test
    public void testCreateJSONObjectFromKeyValue() throws Exception {
        JSONObject obj = JSONHelper.createJSONObject("key", "value");
        assertTrue("JSONObject was created", obj != null);
        assertEquals("JSONObject should have key 'key' with value 'value'", obj.optString("key"), "value");

       // obj = JSONHelper.createJSONObject("key", null);
       // assertTrue("JSONObject was created", obj != null);
       // assertEquals("JSONObject should have key 'key' with empty value", obj.optString("key"), "");

        obj = JSONHelper.createJSONObject(null, "value");
        assertTrue("JSONObject was created", obj != null);
        assertEquals("JSONObject should be empty", obj.length(), 0);
    }

    @Test
    public void testGetJSONObjectWithNoObject() throws Exception {
        JSONObject obj = JSONHelper.createJSONObject("key", "value");
        assertEquals("JSONObject 'key' should return <null> as it's not an object", JSONHelper.getJSONObject(obj, "key"), null);
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

    @Test
    public void testMergeNull() {
        JSONObject result = JSONHelper.merge(null, null);
        assertTrue("Result should be empty object", result.length() == 0);
    }

    @Test
    public void testMergeEmpty() {
        JSONObject result = JSONHelper.merge(new JSONObject(), new JSONObject());
        assertTrue("Result should be empty object", result.length() == 0);
    }

    @Test
    public void testMergeNullBaseData() {
        JSONObject override = JSONHelper.createJSONObject("test", "jee");
        JSONObject result = JSONHelper.merge(null, override);
        assertTrue("Result should be empty object", JSONHelper.isEqual(override, result));
    }

    @Test
    public void testMergeNullOverride() {
        JSONObject base = JSONHelper.createJSONObject("test", "jee");
        JSONObject result = JSONHelper.merge(base, null);
        assertTrue("Result should be empty object", JSONHelper.isEqual(base, result));
    }

    @Test
    public void testMergeOverride() {
        JSONObject base = JSONHelper.createJSONObject("test", "jee");
        JSONObject override = JSONHelper.createJSONObject("test", "moi");
        JSONObject result = JSONHelper.merge(base, override);
        assertFalse("Result shouldn't match basedata", JSONHelper.isEqual(base, result));
        assertTrue("Result should match override", JSONHelper.isEqual(override, result));
        assertTrue("Basedata should not have been modified", JSONHelper.getStringFromJSON(base, "test", "").equals("jee"));
        assertTrue("Overrides should not have been modified", JSONHelper.getStringFromJSON(override, "test", "").equals("moi"));
        assertTrue("Result should have key 'test' with value from override", JSONHelper.getStringFromJSON(result, "test", "").equals("moi"));
    }

    @Test
    public void testMergeOverridePartial() {
        JSONObject base = JSONHelper.createJSONObject("test", "jee");
        JSONObject override = JSONHelper.createJSONObject("moi", "moi");
        JSONObject result = JSONHelper.merge(base, override);
        assertTrue("Basedata should have one key", base.length() == 1);
        assertTrue("Basedata should not have been modified", JSONHelper.getStringFromJSON(base, "test", "").equals("jee"));
        assertTrue("Overrides should have one key", override.length() == 1);
        assertTrue("Overrides should not have been modified", JSONHelper.getStringFromJSON(override, "moi", "").equals("moi"));
        assertTrue("Result should have two keys", result.length() == 2);
        assertTrue("Result should have key 'test'", JSONHelper.getStringFromJSON(result, "test", "").equals("jee"));
        assertTrue("Result should have key 'moi'", JSONHelper.getStringFromJSON(result, "moi", "").equals("moi"));
    }
}

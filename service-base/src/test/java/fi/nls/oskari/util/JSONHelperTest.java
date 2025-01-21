package fi.nls.oskari.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author SMAKINEN
 */
public class JSONHelperTest {
    @Test
    public void testCreateJSONObjectFromString() {
        JSONObject obj = JSONHelper.createJSONObject("{key: \"value\"}");
        Assertions.assertTrue(obj != null, "JSONObject was created");
        Assertions.assertEquals(obj.optString("key"), "value", "JSONObject should have key 'key' with value 'value'");

        obj = JSONHelper.createJSONObject("{ \"key\": \"value\"}");
        Assertions.assertTrue(obj != null, "JSONObject was created");
        Assertions.assertEquals(obj.optString("key"), "value", "JSONObject should have key 'key' with value 'value'");

        obj = JSONHelper.createJSONObject("{}");
        Assertions.assertTrue(obj != null, "JSONObject was created");
        Assertions.assertEquals(obj.length(), 0, "JSONObject should be empty");
    }

    @Test
    public void testCreateJSONObjectFromEmptyString() {

        JSONObject obj = JSONHelper.createJSONObject("");
        Assertions.assertTrue(obj == null, "JSONObject was not created");

        obj = JSONHelper.createJSONObject(null);
        Assertions.assertTrue(obj == null, "JSONObject was not created");
        // TODO: more error cases
    }

    @Test
    public void testCreateJSONObjectFromNullTokener() {

        JSONObject obj = JSONHelper.createJSONObject4Tokener(null);
        Assertions.assertTrue(obj == null, "JSONObject was not created");


    }
    @Test
    public void testCreateJSONObjectFromKeyValue() throws Exception {
        JSONObject obj = JSONHelper.createJSONObject("key", "value");
        Assertions.assertTrue(obj != null, "JSONObject was created");
        Assertions.assertEquals(obj.optString("key"), "value", "JSONObject should have key 'key' with value 'value'");

       // obj = JSONHelper.createJSONObject("key", null);
       // assertTrue("JSONObject was created", obj != null);
       // assertEquals("JSONObject should have key 'key' with empty value", obj.optString("key"), "");

        obj = JSONHelper.createJSONObject(null, "value");
        Assertions.assertTrue(obj != null, "JSONObject was created");
        Assertions.assertEquals(obj.length(), 0, "JSONObject should be empty");
    }

    @Test
    public void testGetJSONObjectWithNoObject() throws Exception {
        JSONObject obj = JSONHelper.createJSONObject("key", "value");
        Assertions.assertEquals(JSONHelper.getJSONObject(obj, "key"), null, "JSONObject 'key' should return <null> as it's not an object");
    }

    @Test
    public void testGetJSONObject() throws Exception {
        JSONObject obj = JSONHelper.createJSONObject("key", "value");
        JSONObject innerObj = JSONHelper.createJSONObject("innerkey", "innervalue");
        Assertions.assertTrue(JSONHelper.putValue(obj, "inner", innerObj), "Inserting inner object should be successful");
        Assertions.assertEquals(JSONHelper.getJSONObject(obj, "inner"), innerObj, "JSONObject 'inner' should return innerObj");
    }
    @Test
    public void testNullValueForGetObjectAsMap() throws Exception {
        String input = "{'first':'a','second':null}".replace('\'', '"');
        JSONObject obj = JSONHelper.createJSONObject(input);

        Map<String, Object> map = JSONHelper.getObjectAsMap(obj);
        Assertions.assertNull(map.get("second"), "Should have null value");

        String fromJSONObject = obj.toString();
        Assertions.assertEquals(input, fromJSONObject, "toString() should match");

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        String fromMapByJackson = mapper.writeValueAsString(map);
        Assertions.assertEquals(input, fromMapByJackson, "Jackson output should match");
    }

    @Test
    public void testMergeNull() {
        JSONObject result = JSONHelper.merge(null, null);
        Assertions.assertTrue(result.length() == 0, "Result should be empty object");
    }

    @Test
    public void testMergeEmpty() {
        JSONObject result = JSONHelper.merge(new JSONObject(), new JSONObject());
        Assertions.assertTrue(result.length() == 0, "Result should be empty object");
    }

    @Test
    public void testMergeNullBaseData() {
        JSONObject override = JSONHelper.createJSONObject("test", "jee");
        JSONObject result = JSONHelper.merge(null, override);
        Assertions.assertTrue(JSONHelper.isEqual(override, result), "Result should be empty object");
    }

    @Test
    public void testMergeNullOverride() {
        JSONObject base = JSONHelper.createJSONObject("test", "jee");
        JSONObject result = JSONHelper.merge(base, null);
        Assertions.assertTrue(JSONHelper.isEqual(base, result), "Result should be empty object");
    }

    @Test
    public void testMergeOverride() {
        JSONObject base = JSONHelper.createJSONObject("test", "jee");
        JSONObject override = JSONHelper.createJSONObject("test", "moi");
        JSONObject result = JSONHelper.merge(base, override);
        Assertions.assertFalse(JSONHelper.isEqual(base, result), "Result shouldn't match basedata");
        Assertions.assertTrue(JSONHelper.isEqual(override, result), "Result should match override");
        Assertions.assertTrue(JSONHelper.getStringFromJSON(base, "test", "").equals("jee"), "Basedata should not have been modified");
        Assertions.assertTrue(JSONHelper.getStringFromJSON(override, "test", "").equals("moi"), "Overrides should not have been modified");
        Assertions.assertTrue(JSONHelper.getStringFromJSON(result, "test", "").equals("moi"), "Result should have key 'test' with value from override");
    }

    @Test
    public void testMergeOverridePartial() {
        JSONObject base = JSONHelper.createJSONObject("test", "jee");
        JSONObject override = JSONHelper.createJSONObject("moi", "moi");
        JSONObject result = JSONHelper.merge(base, override);
        Assertions.assertTrue(base.length() == 1, "Basedata should have one key");
        Assertions.assertTrue(JSONHelper.getStringFromJSON(base, "test", "").equals("jee"), "Basedata should not have been modified");
        Assertions.assertTrue(override.length() == 1, "Overrides should have one key");
        Assertions.assertTrue(JSONHelper.getStringFromJSON(override, "moi", "").equals("moi"), "Overrides should not have been modified");
        Assertions.assertTrue(result.length() == 2, "Result should have two keys");
        Assertions.assertTrue(JSONHelper.getStringFromJSON(result, "test", "").equals("jee"), "Result should have key 'test'");
        Assertions.assertTrue(JSONHelper.getStringFromJSON(result, "moi", "").equals("moi"), "Result should have key 'moi'");
    }
}

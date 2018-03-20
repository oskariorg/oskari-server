package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;


public class JSONHelper {
    
    private static Logger log = LogFactory.getLogger(JSONHelper.class);

    public static final JSONObject createJSONObject(final String key, final String value) {
        final JSONObject object = new JSONObject();
        putValue(object, key, value);
        return object;
    }
    public static final JSONObject createJSONObject(final String key, final Object value) {
        final JSONObject object = new JSONObject();
        putValue(object, key, value);
        return object;
    }
    public static final JSONObject createJSONObject(final String key, final JSONObject value) {
        final JSONObject object = new JSONObject();
        putValue(object, key, value);
        return object;
    }

    public static final JSONObject createJSONObject(final String key, final int value) {
        final JSONObject object = new JSONObject();
        putValue(object, key, value);
        return object;
    }
    public static final JSONObject createJSONObject(final String key, final boolean value) {
        final JSONObject object = new JSONObject();
        putValue(object, key, value);
        return object;
    }
    public static final JSONObject createJSONObject(final String key, final JSONArray value) {
        final JSONObject object = new JSONObject();
        putValue(object, key, value);
        return object;
    }
    public static final JSONObject createJSONObject(final String content) {
        try {
            return new JSONObject(content);
        } catch (Exception e) {
            log.info("Error generating JSONObject from ", content);
        }
        return null;
    }
    public static final JSONObject createJSONObject4Tokener(final JSONTokener content) {
        try {
            return new JSONObject(content);
        } catch (Exception e) {
            log.info("Error generating JSONObject from JSONTokener ", content);
        }
        return null;
    }
    public static final JSONObject getJSONObject(final JSONObject content, String key) {
        if(content == null) {
            return null;
        }
        try {
            return content.getJSONObject(key);
        } catch (Exception e) {
            log.info("Couldn't get JSONObject from ", content, " with key =", key);
            return null;
        }
    }
    public static final Object get(final JSONObject content, String key) {
        if(content == null) {
            return null;
        }
        try {
            return content.get(key);
        } catch (Exception e) {
            log.info("Couldn't get Object from ", content, " with key =", key);
            return null;
        }
    }
    public static final JSONObject getJSONObject(final JSONArray content, int key) {
        if(content == null) {
            return null;
        }
        try {
            return content.getJSONObject(key);
        } catch (Exception e) {
            log.warn("Couldn't get JSONObject from ", content, " with key =", key, " - error: ", e);
            return null;
        }
    }
    public static final JSONArray getJSONArray(final JSONObject content, String key) {
        if (content == null) {
            return null;
        }
        try {
            return content.getJSONArray(key);
        } catch (JSONException e) {
            log.info("Couldn't get JSONArray from " + content + " with key = " + key);
            return null;
        }
    }
    public static final <T> Map<String, T> getObjectAsMap(final JSONObject obj) {
        if(obj == null) {
            return Collections.emptyMap();
        }
        Map<String, T> map = new HashMap<String, T>();
        Iterator it = obj.keys();
        while(it.hasNext()) {
            String key = (String)it.next();
            try {
                if (obj.opt(key) instanceof JSONObject){
                    map.put(key, (T) getObjectAsMap((JSONObject) obj.opt(key)));
                }
                else if (obj.opt(key) instanceof JSONArray){
                    map.put(key, (T) getArrayAsList( (JSONArray) obj.opt(key)));
                }
                else {
                    map.put(key, (T) obj.opt(key));
                }
            }
            catch (Exception e) {
                log.error("Couldn't convert JSONObject to Map:", e.getMessage());
            }
        }
        return map;
    }

    public static final <T> List<T> getArrayAsList(final JSONArray array) {
        if(array == null) {
            return Collections.emptyList();
        }
        try {
            List<T> list = new ArrayList<T>(array.length());
            for(int i = 0; i < array.length(); ++i) {
                if (array.opt(i) instanceof JSONObject){
                    list.add((T) getObjectAsMap((JSONObject) array.opt(i)));
                }
                else if (array.opt(i) instanceof JSONArray){
                    list.add((T) getArrayAsList((JSONArray) array.opt(i)));
                }
                else {
                    list.add((T) array.opt(i));
                }
            }
            return list;
        } catch (Exception e) {
            log.error("Couldn't convert JSONArray to List:", e.getMessage());
        }
        return Collections.emptyList();
    }

    public static JSONArray getEmptyIfNull(final JSONArray array) {
        if(array == null) {
            return new JSONArray();
        }
        return array;
    }

    public static boolean getBooleanFromJSON(final JSONObject data, final String key, final boolean defaultValue) {
        try {
            final boolean value = data.getBoolean(key);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public static final String getStringFromJSON(final JSONObject data, final String key, final String defaultValue) {
        if(data == null) {
            return defaultValue;
        }
        try {
            final String value = (String) data.get(key);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }   
    }
    public static final String getStringFromJSON(final JSONObject data, final String defaultValue) {
        try {
            final String value = data.toString();
            if (value != null) {
                return value;
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static final String getStringFromJSON(final JSONArray data, final String defaultValue) {
        try {
            final String value = data.toString();
            if (value != null) {
                return value;
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static final boolean accumulateValue(final JSONObject json, final String key, final Object value) {
        try {
            json.accumulate(key, value);
            return true;
        } catch (Exception ignore) {
            log.warn("Can't put", key, "value", value, "to json");
        }
        return false;
    }

    public static final boolean putValue(final JSONObject json, final String key, final Object value) {
        try {
            json.put(key, value);
            return true;
        } catch (Exception ignore) {
            log.warn("Can't put", key, "value", value, "to json");
        }
        return false;
    }
    
    public static final boolean put(final JSONObject json, final String key, final JSONArray value){
        try {
            json.put(key, value);
            return true;
        } catch (Exception ignore) {
            log.warn("Can't put", key, "value", value, "to json");
        }
        return false;
    }

    public static final Boolean putValue(final JSONObject json, final String key, final boolean value) {
        try {
            json.put(key, value);
            return true;
        } catch (JSONException ignore) {
            log.warn("Can't put", key, "value", value, "to json");
        }
        return false;
    }

    public static final boolean putValue(final JSONObject json, final String key, final long value) {
        try {
            json.put(key, value);
            return true;
        } catch (Exception ignore) {
            log.warn("Can't put", key, "value", value, "to json");
        }
        return false;
    }
    public static final boolean putValue(final JSONObject json, final String key, final Date value) {
        try {
            json.put(key, String.format("%tFT%<tRZ", value));
            return true;
        } catch (Exception ignore) {
            log.warn("Can't put", key, "value", value, "to json");
        }
        return false;
    }
    
    
    public static final boolean putValue(final JSONObject json, final String key, final double value) {
        try {
            json.put(key, value);
            return true;
        } catch (Exception ignore) {
            log.warn("Can't put", key, "value", value, "to json");
        }
        return false;
    }

    /**
     * Returns an empty array if JSONArray couldn't be created if second parameter is true. For false
     * returns null if JSONArray couldn't be created.
     * @param content
     * @param emptyIfNull
     * @return
     */
    public static JSONArray createJSONArray(final String content, boolean emptyIfNull) {
        JSONArray array = null;
        try {
            array = new JSONArray(content);
        } catch (Exception ignore) {}
        if(emptyIfNull) {
            return getEmptyIfNull(array);
        }
        return null;
    }

	public static JSONArray createJSONArray(final String content) {
        try {
            return new JSONArray(content);
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't create JSONArray for " + content );
        }
	}
    public static JSONArray createJSONArray(final Object content) {
        try {
            final JSONArray array = new JSONArray();
            array.put(content);
            return array;
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't create JSONArray for " + content );
        }
    }

    /**
     *
     * @param json
     * @return Array of 1st level keys
     */
    public static JSONArray createJSONArrayJsonKeys(final JSONObject json) {
        try {
            JSONArray jsarray =  new JSONArray();
            Iterator<String> keys = json.keys();
            while(keys.hasNext()){
                String key = keys.next();
                jsarray.put(key);
            }
            return jsarray;
        } catch (Exception e) {
            throw new IllegalArgumentException("Couldn't create JSONArray of Json keys" );
        }
    }

    /**
     * Compares 2 JSONObjects for equality. Ignores property order and only matches on defined properties and property values.
     * @param jsonObject1
     * @param jsonObject2
     * @return
     */
    public static boolean isEqual(final JSONObject jsonObject1, final JSONObject jsonObject2) {
        if (jsonObject1 == null || JSONObject.getNames(jsonObject1) == null) {
            // if object 1 is null or empty -> object 2 should also be null or empty
            return (jsonObject2 == null || JSONObject.getNames(jsonObject2) == null);
        }
        else if(jsonObject2 == null || JSONObject.getNames(jsonObject2) == null) {
            return false;
        }
        final List<String> objectProperties1 =  Arrays.asList(JSONObject.getNames(jsonObject1));
        Collections.sort(objectProperties1);
        final List<String> objectProperties2 =  Arrays.asList(JSONObject.getNames(jsonObject2));
        Collections.sort(objectProperties2);
        // compare sorted propertynames
        if (!objectProperties1.equals(objectProperties2)) {
            log.debug("Object:\n", objectProperties1, "didn't have same properties as:\n", objectProperties2);
            return false;
        }
        try {
            for (String key : objectProperties1) {
                final Object value1 = jsonObject1.get(key);
                final Object value2 = jsonObject2.get(key);
                if (value1 instanceof JSONObject) {
                    if (!(value2 instanceof JSONObject)) {
                        log.debug(value1, "was a JSONObject unlike", value2);
                        return false;
                    }
                    else if (!isEqual((JSONObject) value1, (JSONObject) value2)) {
                        log.debug("JSONObject recursion was not equal");
                        return false;
                    }
                }
                else if (value1 instanceof JSONArray) {
                    if (!(value2 instanceof JSONArray)) {
                        log.debug(value1, "was a JSONArray unlike", value2);
                        return false;
                    }
                    if (!isEqual((JSONArray) value1, (JSONArray) value2)) {
                        log.debug("JSONArrays were not equal");
                        return false;
                    }
                }
                else if (value1 == null) {
                    if (value2 != null) {
                        log.debug("value1 was <null>, but value2 was:" + value2);
                        return false;
                    }
                } else if (value1 instanceof Number && value2 instanceof Number) {
                    double v1 = ((Number) value1).doubleValue();
                    double v2 = ((Number) value2).doubleValue();
                    if (Math.abs(v1-v2) > 1e-6) {
                        log.debug("Values were not equal:", value1, "!=", value2);
                        return false;
                    }
                } else if (!value1.equals(value2)) {
                    log.debug("Values were not equal:", value1, "!=", value2);
                    return false;
                }
            }
        } catch (Exception ex) {
            log.warn(ex, "Error comparing JSONObjects");
            return false;
        }
        return true;
    }
    /**
     * Compares 2 JSONArrays for equality.
     * @param jsonArray1
     * @param jsonArray2
     * @return
     */
    public static boolean isEqual(JSONArray jsonArray1, JSONArray jsonArray2) {
        if(jsonArray1.length() != jsonArray2.length()) {
            return false;
        }
        if(jsonArray1.length() == 0) {
            return true;
        }
        for(int i = 0; i < jsonArray1.length(); ++i) {
            try {
                final Object value1 = jsonArray1.get(i);
                final Object value2 = jsonArray2.get(i);
                if(value1 instanceof JSONObject && value2 instanceof JSONObject) {
                    if(!isEqual((JSONObject) value1, (JSONObject) value2)) {
                        log.debug("Array content was JSONObjects but they were not equal:", value1, "!=", value2);
                        return false;
                    }
                }
                else  if(value1 instanceof JSONArray && value2 instanceof JSONArray) {
                    if(!isEqual((JSONArray) value1, (JSONArray) value2)) {
                        log.debug("Array content was JSONArrays but they were not equal:", value1, "!=", value2);
                        return false;
                    }
                }
                else if(value1 == null && value2 != null) {
                    log.debug("Array1 had <null>, but Array2 had a value:", value2);
                    return false;
                }
                else if(value1 != null && value2 == null) {
                    log.debug("Array1 had value", value1, ", but Array2 had <null>");
                    return false;
                }
                else if(value1 != null && value2 != null && !value1.equals(value2)) {
                    log.debug("Array values didn't match:", value1, value2);
                    return false;
                }
            } catch (Exception ex) {
                log.warn(ex, "Error comparing JSONArrays");
                return false;
            }
        }
        return true;
    }

    /**
     * Overrides values in base data and returns a new object as the merged result.
     * @param baseData
     * @param overrides
     * @return merged result
     */
    public static JSONObject merge(final JSONObject baseData, final JSONObject overrides) {
        if(baseData == null) {
            return merge(new JSONObject(), overrides);
        }
        // copy existing values so we don't leak mutable references
        final JSONObject result = createJSONObject(baseData.toString());
        // TODO: maybe do the same for overrides?

        if(overrides == null || overrides.length() == 0) {
            // JSONObject.getNames() on empty object returns null so early exit here
            return result;
        }
        try {
            for (String key: JSONObject.getNames(overrides)) {
                Object val = overrides.opt(key);
                if (val instanceof JSONObject) {
                    val = merge(result.optJSONObject(key), (JSONObject)val);
                }
                result.put(key, val);
            }
        } catch (Exception ex) {
            log.warn(ex, "Error merging objects from:", overrides, "- to:", baseData);
        }
        return result;
    }

    /**
     * Returns optional String from obj.key
     * JSONObject.optString() returns "null" if the thing behind key is JSONObject$Null
     * @param obj
     * @param key
     */
    public static String optString(JSONObject obj, String key) {
        return optString(obj, key, "");
    }

    /**
     * Returns optional String from obj.key
     * JSONObject.optString() returns "null" if the thing behind key is JSONObject$Null
     * @param obj
     * @param key
     */
    public static String optString(JSONObject obj, String key, String defaultValue) {
        try {
            Object o = obj.get(key);
            if (o != null && o != JSONObject.NULL) {
                return o.toString();
            }
        } catch (JSONException ignore) {}
        return defaultValue;
    }

    /**
     * Returns required String from obj.key
     * JSONObject.optString() returns "null" if the thing behind key is JSONObject$Null
     * @param obj
     * @param key
     * @throws JSONException
     */
    public static String getString(JSONObject obj, String key) throws JSONException {
        Object o = obj.get(key);
        return o == JSONObject.NULL ? null : o.toString();
    }

}

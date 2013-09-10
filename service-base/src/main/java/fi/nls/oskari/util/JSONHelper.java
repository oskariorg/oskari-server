package fi.nls.oskari.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;



public class JSONHelper {
    
    private static Logger log = LogFactory.getLogger(JSONHelper.class);

    public static final JSONObject createJSONObject(final String key, final String value) {
        final JSONObject object = new JSONObject();
        putValue(object, key, value);
        return object;
    }

    public static final JSONObject createJSONObject(final String content) {
        try {
            return new JSONObject(content);
        } catch (JSONException e) {
            throw new RuntimeException("Couldn't create JSONObject for " + content );
        }
    }

    public static final JSONObject getJSONObject(final JSONObject content, String key) {
        try {
            return content.getJSONObject(key);
        } catch (JSONException e) {
            throw new RuntimeException("Couldn't create JSONObject for " + content + "by key = " + key);
        }
    }

    public static JSONArray getEmptyIfNull(final JSONArray array) {
        if(array == null) {
            return new JSONArray();
        }
        return array;
    }
    
    public static final String getStringFromJSON(final JSONObject data, final String key, final String defaultValue) {
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
    
    public static final boolean putValue(final JSONObject json, final String key, final String value) {
        try {
            json.put(key, value);
            return true;
        } catch (Exception ignore) {
            log.warn("Cant put", key, "value", value, "to json");
        }
        return false;
    }

    /*
    // TODO: why not just have value be of type Object and remove all the duplicate methods?
    // Numbers and booleans are handled in a different way compared to, say, strings, so we can't really do obj.toString()
    public static final boolean putValue(final JSONObject json, final String key, final Object value) {
        try {
            json.put(key, value);
            return true;
        } catch (Exception ignore) {
            log.warn("Cant put", key, "value", value, "to json");
        }
        return false;
    } */

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
            json.put(key, value);
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
    
    
    public static final boolean putValue(final JSONObject json, final String key, final JSONArray value) {
        try {
            json.put(key, value);
            return true;
        } catch (Exception ignore) {
            log.warn("Can't put", key, "value", value, "to json");
        }
        return false;
    }

	public static final boolean putValue(final JSONObject json, final String key,
			JSONObject value) {        
			try {
	            json.put(key, value);
	            return true;
	        } catch (Exception ignore) {
	            log.warn("Can't put", key, "value", value, "to json");
	        }
	        return false;
	}

	public static JSONArray createJSONArray(final String content) {
        try {
            return new JSONArray(content);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't create JSONObject for " + content );
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
                }  else if (!value1.equals(value2)) {
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
}

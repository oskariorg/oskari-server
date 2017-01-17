package fi.nls.oskari.control.statistics.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Encapsulates the allowed types for indicators.
 */
public interface IndicatorValue {
    /**
     * @return Puts the value to a JSON object for encoding in JSON.
     * The value is put in to JSON object under a given key:
     * {
     *   ...
     *   "key": indicatorValue
     * }
     * @throws JSONException 
     */
    public void putToJSONObject(JSONObject json, String key) throws JSONException;
}

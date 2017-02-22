package fi.nls.oskari.control.statistics.data;

import org.json.JSONException;
import org.json.JSONObject;

public class IndicatorValueInteger implements IndicatorValue {
    private Long integerValue;
    public IndicatorValueInteger(Long value) {
        this.integerValue = value;
    }
    public Long getLong() {
        return this.integerValue;
    }
    @Override
    public void putToJSONObject(JSONObject json, String key) throws JSONException {
        json.put(key, integerValue);
    }
}

package fi.nls.oskari.control.statistics.data;

import org.json.JSONException;
import org.json.JSONObject;

public class IndicatorValueFloat implements IndicatorValue {
    private Double floatValue;
    public IndicatorValueFloat(Double value) {
        this.floatValue = value;
    }
    public Double getDouble() {
        return this.floatValue;
    }
    @Override
    public void putToJSONObject(JSONObject json, String key) throws JSONException {
        json.put(key, floatValue);
    }
}

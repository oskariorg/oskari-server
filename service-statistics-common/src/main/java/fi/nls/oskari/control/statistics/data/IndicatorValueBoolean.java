package fi.nls.oskari.control.statistics.data;

import org.json.JSONException;
import org.json.JSONObject;

public class IndicatorValueBoolean implements IndicatorValue {
    private boolean booleanValue;
    public IndicatorValueBoolean(boolean value) {
        this.booleanValue = value;
    }
    public boolean getBoolean() {
        return this.booleanValue;
    }
    @Override
    public void putToJSONObject(JSONObject json, String key) throws JSONException {
        json.put(key, booleanValue);
    }
}

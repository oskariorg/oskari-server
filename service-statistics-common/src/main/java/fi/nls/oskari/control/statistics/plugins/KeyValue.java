package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * Created by SMAKINEN on 19.9.2016.
 */
public class KeyValue {
    public String key;
    public String value;

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public Object toJSON() {
        if(getValue() == null) {
            return getKey();
        }
        JSONObject val = JSONHelper.createJSONObject("id", getKey());
        JSONHelper.putValue(val, "name", getValue());
        return val;
    }
}

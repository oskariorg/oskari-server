package fi.nls.oskari.control.statistics.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * Created by SMAKINEN on 19.9.2016.
 */
public class IdNamePair implements Comparable<IdNamePair> {
    public String key;
    public String value;

    public IdNamePair(String key) {
        this(key, null);
    }
    public IdNamePair(@JsonProperty("key") String key,
                      @JsonProperty("value") String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(IdNamePair o) {
        String toCompare = o.getValue();
        if(toCompare == null) {
            toCompare = o.getKey();
        }
        if(getValue() != null) {
            return getValue().compareTo(toCompare);
        }
        return getKey().compareTo(toCompare);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdNamePair)) {
            return false;
        }

        IdNamePair idNamePair = (IdNamePair) o;

        if (!getKey().equals(idNamePair.getKey())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @JsonIgnore
    public Object getValueForJson() {
        if(getValue() == null) {
            return getKey();
        }
        JSONObject val = JSONHelper.createJSONObject("id", getKey());
        JSONHelper.putValue(val, "name", getValue());
        return val;
    }
}

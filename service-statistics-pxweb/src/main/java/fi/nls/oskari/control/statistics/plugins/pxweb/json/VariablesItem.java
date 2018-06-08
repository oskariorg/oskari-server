package fi.nls.oskari.control.statistics.plugins.pxweb.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.control.statistics.data.IdNamePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VariablesItem {
    @JsonProperty("code")
    private String code;
    @JsonProperty("values")
    private List<String> values;
    @JsonProperty("text")
    private String text;
    @JsonProperty("valueTexts")
    private List<String> valueTexts;
    @JsonProperty("time")
    private boolean time;

    public String getCode() {
        return code;
    }

    public boolean isTimeVariable() {
        return time;
    }

    public String getLabel() {
        if(text == null) {
            return getCode();
        }
        return text;
    }

    public List<IdNamePair> getLabels() {
        if(values == null) {
            return Collections.emptyList();
        }
        List<IdNamePair> list = new ArrayList<>(values.size());
        for(int i = 0; i < values.size(); i++) {
            String id = values.get(i);
            IdNamePair pair = new IdNamePair(id, getValueText(i, id));
            list.add(pair);
        }
        return list;
    }

    private String getValueText(int index, String defaultValue) {
        if(index < valueTexts.size()) {
            return valueTexts.get(index);
        }
        return defaultValue;
    }
}
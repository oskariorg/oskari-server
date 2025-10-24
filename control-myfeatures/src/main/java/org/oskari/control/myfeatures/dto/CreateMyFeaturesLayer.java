package org.oskari.control.myfeatures.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMyFeaturesLayer {

    private List<MyFeaturesFieldInfo> layerFields;
    private Map<String, Map<String, Object>> locale;
    private JSONObject style;

    public List<MyFeaturesFieldInfo> getLayerFields() {
        return layerFields;
    }

    public void setLayerFields(List<MyFeaturesFieldInfo> layerFields) {
        this.layerFields = layerFields;
    }

    public Map<String, Map<String, Object>> getLocale() {
        return locale;
    }

    public void setLocale(Map<String, Map<String, Object>> locale) {
        this.locale = locale;
    }

    public JSONObject getStyle() {
        return style;
    }

    public void setStyle(JSONObject style) {
        this.style = style;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (layerFields == null || layerFields.isEmpty()) {
            errors.add("layerFields must be non-null and non-empty");
        }

        if (locale == null || locale.isEmpty()) {
            errors.add("locale must be non-null and non-empty");
        }

        return errors;
    }

}

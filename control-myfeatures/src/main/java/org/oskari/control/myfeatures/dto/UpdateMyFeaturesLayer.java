package org.oskari.control.myfeatures.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMyFeaturesLayer {

    private String id;
    private Map<String, Map<String, Object>> locale;
    private Map<String, Object> style;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Map<String, Object>> getLocale() {
        return locale;
    }

    public void setLocale(Map<String, Map<String, Object>> locale) {
        this.locale = locale;
    }

    public Map<String, Object> getStyle() {
        return style;
    }

    public void setStyle(Map<String, Object> style) {
        this.style = style;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (id == null || id.isBlank()) {
            errors.add("id is required");
        } else if (MyFeaturesLayer.parseLayerId(id).isEmpty()) {
            errors.add("id unexpected format");
        }

        if (locale == null || locale.isEmpty()) {
            errors.add("locale must be non-null and non-empty");
        }

        return errors;
    }

}

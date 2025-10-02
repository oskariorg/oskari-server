package org.oskari.control.myfeatures.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.util.JSONHelper;

public class CreateMyFeaturesLayer {

    private Map<String, Map<String, String>> locale;
    private List<MyFeaturesFieldInfo> layerFields;
    private Integer opacity;
    private Map<String, Object> layerOptions;
    private Map<String, Object> layerAttributes;

    public Map<String, Map<String, String>> getLocale() {
        return locale;
    }

    public void setLocale(Map<String, Map<String, String>> locale) {
        this.locale = locale;
    }

    public List<MyFeaturesFieldInfo> getLayerFields() {
        return layerFields;
    }

    public void setLayerFields(List<MyFeaturesFieldInfo> layerFields) {
        this.layerFields = layerFields;
    }

    public Integer getOpacity() {
        return opacity;
    }

    public void setOpacity(Integer opacity) {
        this.opacity = opacity;
    }

    public Map<String, Object> getLayerOptions() {
        return layerOptions;
    }

    public void setLayerOptions(Map<String, Object> layerOptions) {
        this.layerOptions = layerOptions;
    }

    public Map<String, Object> getLayerAttributes() {
        return layerAttributes;
    }

    public void setLayerAttributes(Map<String, Object> layerAttributes) {
        this.layerAttributes = layerAttributes;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (locale == null || locale.isEmpty()) {
            errors.add("locale must be non-null and non-empty");
        }
        if (layerFields.isEmpty()) {
            errors.add("layerFields must be non-null and non-empty");
        }
        if (opacity != null && (opacity < 0 || opacity > 100)) {
            errors.add("opacity must be within [0, 100]");
        }

        return errors;
    }

    public MyFeaturesLayer toDomain(ObjectMapper om) throws Exception {
        MyFeaturesLayer layer = new MyFeaturesLayer();

        layer.setLocale(JSONHelper.createJSONObject(om.writeValueAsString(locale)));
        layer.setLayerFields(layerFields);

        if (opacity != null) {
            layer.setOpacity(opacity);
        }
        if (layerOptions != null) {
            layer.setOptions(JSONHelper.createJSONObject(om.writeValueAsString(layerOptions)));
        }
        if (layerAttributes != null) {
            layer.setAttributes(JSONHelper.createJSONObject(om.writeValueAsString(layerAttributes)));
        }

        return layer;
    }

}

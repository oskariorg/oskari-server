package org.oskari.control.myfeatures.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;

public class MyFeaturesLayerInfo {

    private UUID id;
    private Map<String, Map<String, Object>> locale;
    private String ownerUuid;
    private Instant created;
    private Instant updated;

    private List<MyFeaturesFieldInfo> layerFields;
    private int featureCount;
    private double[] extent;

    // No simple model for these yet
    private JSONObject layerOptions;
    private JSONObject layerAttributes;

    public UUID getId() {
        return id;
    }

    public Map<String, Map<String, Object>> getLocale() {
        return locale;
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getUpdated() {
        return updated;
    }

    public List<MyFeaturesFieldInfo> getLayerFields() {
        return layerFields;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public double[] getExtent() {
        return extent;
    }

    public JSONObject getLayerOptions() {
        return layerOptions;
    }

    public JSONObject getLayerAttributes() {
        return layerAttributes;
    }

    public static MyFeaturesLayerInfo from(MyFeaturesLayer layer) {
        MyFeaturesLayerInfo info = new MyFeaturesLayerInfo();
        info.id = layer.getId();
        info.locale = layer.getLocale() == null ? null : layer.getLocale().keySet().stream()
            .collect(Collectors.toMap(lang -> lang, lang -> layer.getLocale().getJSONObject(lang).toMap()));
        info.ownerUuid = layer.getOwnerUuid();
        info.created = layer.getCreated();
        info.updated = layer.getUpdated();
        info.layerFields = layer.getLayerFields();
        info.featureCount = layer.getFeatureCount();
        info.extent = from(layer.getExtent());
        info.layerOptions = layer.getOptions();
        info.layerAttributes = layer.getAttributes();
        return info;
    }

    private static double[] from(Envelope e) {
        if (e == null) {
            return null;
        }
        return new double[] { e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY() };
    }
}

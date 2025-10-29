package fi.nls.oskari.domain.map.myfeatures;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class MyFeaturesLayerFullInfo {

    private String id;
    private String type = "myf";
    private Instant created;
    private Instant updated;
    private int featureCount;
    private int opacity;
    // No actual model for these (yet)
    private JSONObject options;
    private JSONObject attributes;
    private Map<String, Map<String, Object>> locale;
    private List<MyFeaturesFieldInfo> layerFields;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getUpdated() {
        return updated;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public int getOpacity() {
        return opacity;
    }

    public JSONObject getOptions() {
        return options;
    }

    public JSONObject getAttributes() {
        return attributes;
    }

    public Map<String, Map<String, Object>> getLocale() {
        return locale;
    }

    public List<MyFeaturesFieldInfo> getLayerFields() {
        return layerFields;
    }

    public static MyFeaturesLayerFullInfo from(MyFeaturesLayer layer) {
        MyFeaturesLayerFullInfo info = new MyFeaturesLayerFullInfo();
        info.id = info.type + "_" + layer.getId();
        info.created = layer.getCreated();
        info.updated = layer.getUpdated();
        info.featureCount = layer.getFeatureCount();
        info.opacity = layer.getOpacity();
        info.options = layer.getOptions();
        info.attributes = layer.getAttributes();
        info.locale = layer.getLocale() == null ? null : layer.getLocale().keySet().stream()
                .collect(Collectors.toMap(lang -> lang, lang -> layer.getLocale().getJSONObject(lang).toMap()));
        info.layerFields = layer.getLayerFields();
        return info;
    }
}

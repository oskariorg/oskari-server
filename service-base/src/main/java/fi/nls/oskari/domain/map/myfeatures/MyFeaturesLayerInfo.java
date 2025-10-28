package fi.nls.oskari.domain.map.myfeatures;

import java.time.Instant;

import org.json.JSONObject;

public class MyFeaturesLayerInfo {

    private String id;
    private String type;
    private String name;
    private String subtitle;
    private Instant created;
    private Instant updated;
    private int featureCount;
    // No actual model for these (yet)
    private JSONObject options;
    private JSONObject attributes;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
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

    public JSONObject getOptions() {
        return options;
    }

    public JSONObject getAttributes() {
        return attributes;
    }

    public static MyFeaturesLayerInfo from(MyFeaturesLayer layer, String lang) {
        return from(layer, lang, "myf");
    }

    public static MyFeaturesLayerInfo from(MyFeaturesLayer layer, String lang, String type) {
        MyFeaturesLayerInfo info = new MyFeaturesLayerInfo();
        info.id = MyFeaturesLayer.prefixLayerId(layer.getId());
        info.type = type;
        info.name = layer.getName(lang);
        info.subtitle = layer.getDesc(lang);
        info.created = layer.getCreated();
        info.updated = layer.getUpdated();
        info.featureCount = layer.getFeatureCount();
        info.options = layer.getOptions();
        info.attributes = layer.getAttributes();
        return info;
    }

}

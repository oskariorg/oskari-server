package fi.nls.oskari.domain.map.myfeatures;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;

import fi.nls.oskari.domain.map.JSONLocalizedName;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;

public class MyFeaturesLayer extends JSONLocalizedName {

    private static final String LOCALE_DESC = "desc";
    private static final String LOCALE_SOURCE = "source";

    private UUID id;
    private String ownerUuid;
    private OffsetDateTime created;
    private OffsetDateTime updated;

    private List<MyFeaturesFieldInfo> layerFields;
    private int featureCount;
    private Envelope extent;

    private int opacity = 80;
    private boolean published;

    private WFSLayerOptions layerOptions;
    private WFSLayerAttributes layerAttributes;


    public final String getType() {
        return OskariLayer.TYPE_MYFEATURES;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPrefixedId() {
        return getType() + "_" + getId();
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    public String getDesc(final String language) {
        return getLocalizedValue(language, LOCALE_DESC);
    }

    public void setDesc(final String language, final String desc) {
        setLocalizedValue(language, LOCALE_DESC, desc);
    }

    public String getSource(final String language) {
        return getLocalizedValue(language, LOCALE_SOURCE);
    }

    public void setSource(final String language, final String source) {
        setLocalizedValue(language, LOCALE_SOURCE, source);
    }

    public List<MyFeaturesFieldInfo> getLayerFields() {
        return layerFields;
    }

    public void setLayerFields(List<MyFeaturesFieldInfo> fields) {
        this.layerFields = fields;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(int featureCount) {
        this.featureCount = featureCount;
    }

    public Envelope getExtent() {
        return extent;
    }

    public void setExtent(Envelope extent) {
        this.extent = extent;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean getPublished() {
        return isPublished();
    }

    public boolean isPublished() {
        return published;
    }

    public WFSLayerOptions getLayerOptions() {
        if (layerOptions == null) {
            setLayerOptions(new WFSLayerOptions());
        }
        return layerOptions;
    }

    public void setLayerOptions(WFSLayerOptions layerOptions) {
        this.layerOptions = layerOptions;
    }

    public WFSLayerAttributes getLayerAttributes() {
        if (layerAttributes == null) {
            setLayerAttributes(new WFSLayerAttributes(new JSONObject()));
        }
        return layerAttributes;
    }

    public void setLayerAttributes(WFSLayerAttributes layerAttributes) {
        this.layerAttributes = layerAttributes;
    }

    /* Helpers operating with JSON */

    public JSONArray getFields() {
        List<MyFeaturesFieldInfo> fields = getLayerFields();
        if (fields == null) {
            return new JSONArray();
        }
        return new JSONArray(fields.stream()
                .map(f -> new JSONObject()
                        .put("name", f.getName())
                        .put("type", f.getType()))
                .collect(Collectors.toList()));
    }

    public void setFields(JSONArray fields) {
        int n = fields.length();
        List<MyFeaturesFieldInfo> fieldInfos = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            JSONObject info = fields.getJSONObject(i);
            fieldInfos.add(toFieldInfo(info));
        }
        setLayerFields(fieldInfos);
    }

    private static MyFeaturesFieldInfo toFieldInfo(JSONObject info) {
        String name = info.getString("name");
        String type = info.getString("type");
        try {
            return MyFeaturesFieldInfo.of(name, MyFeaturesFieldType.valueOf(type));
        } catch (Exception e) {
            // This won't happen...
            throw new RuntimeException(e);
        }
    }

    public JSONObject getOptions() {
        return getLayerOptions().getOptions();
    }

    public void setOptions(JSONObject options) {
        setLayerOptions(new WFSLayerOptions(options));
    }

    public JSONObject getAttributes() {
        return getLayerAttributes().getAttributes();
    }

    public void setAttributes(JSONObject attributes) {
        setLayerAttributes(new WFSLayerAttributes(attributes));
    }

}

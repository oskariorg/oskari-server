package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LayerCapabilitiesWMS extends LayerCapabilitiesOGC {

    public static final String DESCRIPTION = "desc";
    public static final String KEYWORDS = "keywords";
    public static final String BBOX = "bbox";
    public static final String LAYERS = "layers";
    public static final String PARENT = "parent";
    public static final String MIN_SCALE = "minScale";
    public static final String MAX_SCALE = "maxScale";
    public static final String METADATA_URL = "metadataUrl";

    public LayerCapabilitiesWMS(String name, String title) {
        super(name, title);
    }

    @JsonIgnore
    public boolean isGroupLayer() {
        return this.getName() == null;
    }

    public void setParent(String parent) {
        if (parent != null) {
            addCapabilityData(PARENT, parent);
        }
    }
    public void setMetadataUrl(String url) {
        if (url != null) {
            addCapabilityData(METADATA_URL, url);
        }
    }
    public void setMinScale(String scale) {
        if (scale == null) {
            return;
        }
        try {
            addCapabilityData(MIN_SCALE, Double.parseDouble(scale));
        } catch (Exception ignored) {}
    }
    public void setMaxScale(String scale) {
        if (scale == null) {
            return;
        }
        try {
            addCapabilityData(MAX_SCALE, Double.parseDouble(scale));
        } catch (Exception ignored) {}
    }

    @JsonIgnore
    public String getParent() {
        return (String) getTypeSpecific().get(PARENT);
    }

    public void setDescription(String description) {
        if (description != null) {
            addCapabilityData(DESCRIPTION, description);
        }
    }

    @JsonIgnore
    public String getDescription() {
        return (String) getTypeSpecific().get(DESCRIPTION);
    }
    public void setKeywords(Set<String> words) {
        addCapabilityData(KEYWORDS, words);
    }

    @JsonIgnore
    public Set<String> getKeywords() {
        return (Set<String>) getTypeSpecific().getOrDefault(KEYWORDS, Collections.emptySet());
    }
    public void setBbox(BoundingBox bbox) {
        if (bbox != null) {
            addCapabilityData(BBOX, bbox);
        }
    }

    @JsonIgnore
    public BoundingBox getBbox() {
        return (BoundingBox) getTypeSpecific().get(BBOX);
    }

    public void setLayers(List<LayerCapabilitiesWMS> layers) {
        if (layers == null) {
            return;
        }
        // make children without styles inherit parent styles
        propagateStyles(layers, getStyles());
        addCapabilityData(LAYERS, layers);
    }

    private void propagateStyles(List<LayerCapabilitiesWMS> layers, List<LayerStyle> styles) {
        if (layers == null) {
            return;
        }
        layers.stream().forEach(l -> {
            if (l.getStyles().isEmpty()) {
                l.setStyles(styles);
            }
            propagateStyles(l.getLayers(), l.getStyles());
        });
    }

    @JsonIgnore
    public List<LayerCapabilitiesWMS> getLayers() {
        return (List<LayerCapabilitiesWMS>) getTypeSpecific().getOrDefault(LAYERS, Collections.emptyList());
    }
}

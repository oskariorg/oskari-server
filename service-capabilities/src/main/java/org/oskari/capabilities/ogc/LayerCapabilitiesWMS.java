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

    public LayerCapabilitiesWMS(String name, String title) {
        super(name, title);
    }

    @JsonIgnore
    public boolean isGroupLayer() {
        return this.getName() == null;
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

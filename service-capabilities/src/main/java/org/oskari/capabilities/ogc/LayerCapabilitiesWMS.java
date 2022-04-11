package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.nls.oskari.domain.map.OskariLayer;

import java.util.Collections;
import java.util.List;

public class LayerCapabilitiesWMS extends LayerCapabilitiesOGC {

    public static final String LAYERS = "layers";
    public static final String PARENT = "parent";
    public static final String MIN_SCALE = "minScale";
    public static final String MAX_SCALE = "maxScale";
    public static final String TIMES = "times";

    public LayerCapabilitiesWMS(String name, String title) {
        super(name, title);
        setType(OskariLayer.TYPE_WMS);
    }

    @JsonIgnore
    public boolean isGroupLayer() {
        return this.getName() == null;
    }

    public void setTimes(Object times) {
        if (times != null) {
            addCapabilityData(TIMES, times);
        }
    }

    public void setParent(String parent) {
        if (parent != null) {
            addCapabilityData(PARENT, parent);
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

    @JsonIgnore
    public Double getMinScale() {
        return (Double) getTypeSpecific().get(MIN_SCALE);
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
    public Double getMaxScale() {
        return (Double) getTypeSpecific().get(MAX_SCALE);
    }

    @JsonIgnore
    public String getParent() {
        return (String) getTypeSpecific().get(PARENT);
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

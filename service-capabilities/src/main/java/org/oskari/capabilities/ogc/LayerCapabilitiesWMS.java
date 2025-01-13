package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.domain.map.OskariLayer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LayerCapabilitiesWMS extends LayerCapabilitiesOGC {

    public static final String TIMES = "times";
    private List<LayerCapabilitiesWMS> sublayers;
    private String parent;
    private Set<String> infoFormats;
    private String[] times;
    private Double maxScale;
    private Double minScale;

    public LayerCapabilitiesWMS(@JsonProperty("name") String name, @JsonProperty("title") String title) {
        super(name, title);
        setType(OskariLayer.TYPE_WMS);
    }

    public List<LayerCapabilitiesWMS> getLayers() {
        if (sublayers == null) {
            return Collections.emptyList();
        }
        return sublayers;
    }

    public void setLayers(List<LayerCapabilitiesWMS> layers) {
        sublayers = layers;
        if (layers == null) {
            return;
        }
        // make children without styles inherit parent styles
        propagateStyles(layers, getStyles());
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

    public Set<String> getInfoFormats() {
        if (infoFormats == null) {
            return Collections.emptySet();
        }
        return infoFormats;
    }

    public void setInfoFormats(Set<String> infoFormats) {
        this.infoFormats = infoFormats;
    }

    public boolean isQueryable() {
        return !getInfoFormats().isEmpty();
    }

    @JsonIgnore
    public boolean isGroupLayer() {
        return this.getName() == null;
    }

    public String[] getTimes() {
        return times;
    }

    public void setTimes(String[] times) {
        this.times = times;
    }

    public Double getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(String maxScale) {
        try {
            setMaxScale(Double.parseDouble(maxScale));
        } catch (Exception ignored) {
        }
    }

    public void setMaxScale(Double maxScale) {
        this.maxScale = maxScale;
        if (this.maxScale == 0) {
            this.maxScale = -1d;
        }
    }

    public Double getMinScale() {
        return minScale;
    }

    public void setMinScale(String minScale) {
        try {
            setMinScale(Double.parseDouble(minScale));
        } catch (Exception ignored) {
        }
    }

    public void setMinScale(Double minScale) {
        this.minScale = minScale;
        if (this.minScale == 0) {
            this.minScale = -1d;
        }
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}

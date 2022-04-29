package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.domain.map.OskariLayer;
import org.oskari.capabilities.ogc.wfs.FeaturePropertyType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.oskari.capabilities.ogc.CapabilitiesConstants.INFO_FORMATS;
import static org.oskari.capabilities.ogc.CapabilitiesConstants.IS_QUERYABLE;

public class LayerCapabilitiesWMS extends LayerCapabilitiesOGC {

    public static final String PARENT = "parent";
    public static final String MIN_SCALE = "minScale";
    public static final String MAX_SCALE = "maxScale";
    public static final String TIMES = "times";
    private List<LayerCapabilitiesWMS> sublayers;
    private Set<String> infoFormats;

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
    public void setInfoFormats(Set<String> infoFormats) {
        this.infoFormats = infoFormats;
    }

    public Set<String> getInfoFormats() {
        if (infoFormats == null) {
            return Collections.emptySet();
        }
        return infoFormats;
    }
    public boolean isQueryable() {
        return !getInfoFormats().isEmpty();
    }

    @JsonIgnore
    public boolean isGroupLayer() {
        return this.getName() == null;
    }

    public void setTimes(Object times) {
        addCapabilityData(TIMES, times);
    }

    @JsonIgnore
    public Double getMinScale() {
        return (Double) getTypeSpecific().get(MIN_SCALE);
    }

    public void setMinScale(String scale) {
        if (scale == null) {
            return;
        }
        try {
            addCapabilityData(MIN_SCALE, Double.parseDouble(scale));
        } catch (Exception ignored) {
        }
    }

    @JsonIgnore
    public Double getMaxScale() {
        return (Double) getTypeSpecific().get(MAX_SCALE);
    }

    public void setMaxScale(String scale) {
        if (scale == null) {
            return;
        }
        try {
            addCapabilityData(MAX_SCALE, Double.parseDouble(scale));
        } catch (Exception ignored) {
        }
    }

    @JsonIgnore
    public String getParent() {
        return (String) getTypeSpecific().get(PARENT);
    }

    public void setParent(String parent) {
        addCapabilityData(PARENT, parent);
    }

}

package org.oskari.capabilities;

import org.oskari.capabilities.ogc.LayerStyle;

import java.util.*;

// Something that can be serialized to oskari_maplayer.capabilities
public class LayerCapabilities {

    private String name;
    private String title;
    private List<LayerStyle> styles;
    private Set<String> srs;
    private String defaultStyle;

    private Map<String, Object> typeSpecific = new HashMap<>();

    public LayerCapabilities(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public void setStyles(List<LayerStyle> styles) {
        setStyles(styles, null);
    }
    public void setStyles(List<LayerStyle> styles, String defaultStyle) {
        this.styles = styles;
        this.defaultStyle = defaultStyle;
        if (defaultStyle == null && !styles.isEmpty()) {
            this.defaultStyle = styles.get(0).getName();
        }
    }
    public void setSrs(Set<String> supported) {
        this.srs = supported;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public List<LayerStyle> getStyles() {
        if (styles == null) {
            return Collections.emptyList();
        }
        return styles;
    }

    public Set<String> getSrs() {
        if (srs == null) {
            return Collections.emptySet();
        }
        return srs;
    }

    public String getDefaultStyle() {
        if (defaultStyle == null && styles != null && !styles.isEmpty()) {
            return styles.get(0).getName();
        }
        return defaultStyle;
    }

    public Map<String, Object> getTypeSpecific() {
        return typeSpecific;
    }

    public void addCapabilityData(String key, Object value) {
        typeSpecific.put(key, value);
    }
}

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

    private Map<String, Object> layerSpecific = new HashMap<>();
    /*
    private Set<String> formats;
    private Set<String> infoFormats;
    private List<ResourceUrl> resourceUrls;
    private List<TileMatrixLink> links;
    */
    // TODO: add more fields that will be parsed

    public LayerCapabilities(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public void setStyles(List<LayerStyle> styles, String defaultStyle) {
        this.styles = styles;
        this.defaultStyle = defaultStyle;
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
        return defaultStyle;
    }

    public Map<String, Object> getLayerSpecific() {
        return layerSpecific;
    }

    public void addLayerSpecific(String key, Object value) {
        layerSpecific.put(key, value);
    }
}

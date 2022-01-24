package org.oskari.capabilities;

import org.oskari.capabilities.ogc.LayerStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Something that can be serialized to oskari_maplayer.capabilities
public class LayerCapabilities {

    private String name;
    private String title;
    private List<LayerStyle> styles;
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


    public String getName() {
        return name;
    }

    public void addLayerSpecific(String key, Object value) {
        layerSpecific.put(key, value);
    }
}

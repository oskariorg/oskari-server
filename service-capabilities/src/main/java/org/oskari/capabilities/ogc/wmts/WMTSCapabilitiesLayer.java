package org.oskari.capabilities.ogc.wmts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.nls.oskari.domain.map.OskariLayer;
//import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.capabilities.ogc.LayerStyle;

import java.util.List;
import java.util.Set;

public class WMTSCapabilitiesLayer {

    private final String id;
    private final String title;
    private final List<LayerStyle> styles;
    private final String defaultStyle;
    private final Set<String> formats;
    private final Set<String> infoFormats;
    private final List<ResourceUrl> resourceUrls;
    private final List<TileMatrixLink> links;

    public WMTSCapabilitiesLayer(String id, String title,
            List<LayerStyle> styles, String defaultStyle,
            Set<String> formats, Set<String> infoFormats,
            List<ResourceUrl> resourceUrls,
            List<TileMatrixLink> links) {
        this.id = id;
        this.title = title;
        this.styles = styles;
        this.defaultStyle = defaultStyle;
        this.formats = formats;
        this.infoFormats = infoFormats;
        this.resourceUrls = resourceUrls;
        this.links = links;
    }

    public List<ResourceUrl> getResourceUrls() {
        return resourceUrls;
    }

    @JsonIgnore
    public ResourceUrl getResourceUrlByType(final String type) {
        for (ResourceUrl url : resourceUrls) {
            if (url.getType().equalsIgnoreCase(type)) {
                return url;
            }
        }
        return null;
    }

    public boolean isQueryable() {
        return infoFormats.size() > 0;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public List<LayerStyle> getStyles() {
        return styles;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public Set<String> getFormats() {
        return formats;
    }

    public Set<String> getInfoFormats() {
        return infoFormats;
    }

    public List<TileMatrixLink> getLinks() {
        return links;
    }

    @JsonIgnore
    public List<TileMatrixLimits> getLimits(String tileMatrixSet) {
        for (TileMatrixLink link : links) {
            if (tileMatrixSet.equals(link.getTileMatrixSet().getId())) {
                return link.getLimits();
            }
        }
        return null;
    }

}

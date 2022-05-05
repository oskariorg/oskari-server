package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.domain.map.OskariLayer;
import org.oskari.capabilities.ogc.wmts.ResourceUrl;
import org.oskari.capabilities.ogc.wmts.TileMatrixLink;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LayerCapabilitiesWMTS extends LayerCapabilitiesOGC {

    private List<ResourceUrl> resourceUrls;
    private List<TileMatrixLink> links;
    private Set<String> infoFormats;

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

    public LayerCapabilitiesWMTS(@JsonProperty("name") String name, @JsonProperty("title") String title) {
        super(name, title);
        setType(OskariLayer.TYPE_WMTS);
    }

    public List<ResourceUrl> getResourceUrls() {
        if (resourceUrls == null) {
            return Collections.emptyList();
        }
        return resourceUrls;
    }

    public void setResourceUrls(List<ResourceUrl> urls) {
        resourceUrls = urls;
    }

    public ResourceUrl getResourceUrl(String type) {
        return getResourceUrls().stream()
                .filter(url -> "tile".equals(url.getType()))
                .findFirst().orElse(null);
    }

    public List<TileMatrixLink> getTileMatrixLinks() {
        if (links == null) {
            return Collections.emptyList();
        }
        return links;
    }

    public void setTileMatrixLinks(List<TileMatrixLink> links) {
        this.links = links;
    }
}

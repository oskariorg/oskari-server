package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.nls.oskari.domain.map.OskariLayer;
import org.oskari.capabilities.ogc.wmts.ResourceUrl;
import org.oskari.capabilities.ogc.wmts.TileMatrixLink;

import java.util.Collections;
import java.util.List;

public class LayerCapabilitiesWMTS extends LayerCapabilitiesOGC {

    public static final String RESOURCE_URLS = "resourceUrls";
    public static final String TILEMATRIX = "tileMatrix";

    public LayerCapabilitiesWMTS(String name, String title) {
        super(name, title);
        setType(OskariLayer.TYPE_WMTS);
    }
    public void setResourceUrls(List<ResourceUrl> urls) {
        addCapabilityData(RESOURCE_URLS, urls);
    }
    public void setTileMatrices(List<TileMatrixLink> links) {
        addCapabilityData(TILEMATRIX, links);
    }

    @JsonIgnore
    public List<ResourceUrl> getResourceUrls() {
        return (List<ResourceUrl>) getTypeSpecific().getOrDefault(RESOURCE_URLS, Collections.emptyList());
    }
    public ResourceUrl getResourceUrl(String type) {
        return getResourceUrls().stream()
                .filter(url -> "tile".equals(url.getType()))
                .findFirst().orElse(null);
    }
    @JsonIgnore
    public List<TileMatrixLink> getTileMatrices() {
        return (List<TileMatrixLink>) getTypeSpecific().getOrDefault(TILEMATRIX, Collections.emptyList());
    }
}

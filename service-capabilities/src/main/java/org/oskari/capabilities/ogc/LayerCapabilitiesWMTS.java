package org.oskari.capabilities.ogc;

import org.oskari.capabilities.ogc.wmts.ResourceUrl;
import org.oskari.capabilities.ogc.wmts.TileMatrixLink;

import java.util.List;

public class LayerCapabilitiesWMTS extends LayerCapabilitiesOGC {

    public static final String RESOURCE_URLS = "resourceUrls";
    public static final String TILEMATRIX = "tileMatrix";

    public LayerCapabilitiesWMTS(String name, String title) {
        super(name, title);
    }
    public void setResourceUrls(List<ResourceUrl> urls) {
        addCapabilityData(RESOURCE_URLS, urls);
    }
    public void setTileMatrices(List<TileMatrixLink> links) {
        addCapabilityData(TILEMATRIX, links);
    }
}

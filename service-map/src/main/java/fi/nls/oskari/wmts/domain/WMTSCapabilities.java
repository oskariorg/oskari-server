package fi.nls.oskari.wmts.domain;

import java.util.Collection;
import java.util.Map;

public class WMTSCapabilities {

    private final Map<String, TileMatrixSet> tileMatrixSets;
    private final Map<String, WMTSCapabilitiesLayer> layers;

    public WMTSCapabilities(Map<String, TileMatrixSet> tileMatrixSets,
            Map<String, WMTSCapabilitiesLayer> layers) {
        this.tileMatrixSets = tileMatrixSets;
        this.layers = layers;
    }

    public Collection<TileMatrixSet> getTileMatrixSets() {
        return tileMatrixSets.values();
    }

    public Collection<WMTSCapabilitiesLayer> getLayers() {
        return layers.values();
    }

    public WMTSCapabilitiesLayer getLayer(final String name) {
        return layers.get(name);
    }

}

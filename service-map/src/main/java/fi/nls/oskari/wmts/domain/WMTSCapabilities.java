package fi.nls.oskari.wmts.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by SMAKINEN on 28.9.2015.
 */
public class WMTSCapabilities {

    final Set<TileMatrixSet> tileMatrixSets = new HashSet<>();
    final Set<WMTSCapabilitiesLayer> layers = new HashSet<>();

    public void addLayer(WMTSCapabilitiesLayer layer) {
        layers.add(layer);
    }
    public void addTileMatrixSet(TileMatrixSet set) {
        tileMatrixSets.add(set);
    }

    public Set<TileMatrixSet> getTileMatrixSets() {
        return tileMatrixSets;
    }

    public Set<WMTSCapabilitiesLayer> getLayers() {
        return layers;
    }

    public String getMatrixCRS(final String id) {
        for(TileMatrixSet matrix : tileMatrixSets) {
            if(matrix.getId().equals(id)) {
                return matrix.getCrs();
            }
        }
        return "CRS N/A";
    }

    public WMTSCapabilitiesLayer getLayer(final String name) {
        for(WMTSCapabilitiesLayer layer : layers) {
            if(layer.getId().equals(name)) {
                return layer;
            }
        }
        return null;
    }
}

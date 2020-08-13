package org.oskari.maplayer.model;

import fi.nls.oskari.wmts.domain.TileMatrixSet;

import java.util.Collection;

public class ServiceCapabilitiesResultWMTS extends ServiceCapabilitiesResult {

    private Collection<TileMatrixSet> matrixSets;

    public Collection<TileMatrixSet> getMatrixSets() {
        return matrixSets;
    }
    public void setMatrixSets(Collection<TileMatrixSet> matrixSets) {
        this.matrixSets = matrixSets;
    }
}

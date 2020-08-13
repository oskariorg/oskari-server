package org.oskari.maplayer.model;

import java.util.List;

public class ServiceCapabilitiesResultWMS extends ServiceCapabilitiesResult {

    private List<MapLayerStructure> structure;

    public List<MapLayerStructure> getStructure() {
        return structure;
    }
    public void setStructure(List<MapLayerStructure> structure) {
        this.structure = structure;
    }
}

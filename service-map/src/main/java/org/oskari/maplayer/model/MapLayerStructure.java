package org.oskari.maplayer.model;

import java.util.List;

public class MapLayerStructure {
    private String name;
    private List<MapLayerStructure> structure;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MapLayerStructure> getStructure() {
        return structure;
    }
    public void setStructure(List<MapLayerStructure> structure) {
        this.structure = structure;
    }
}

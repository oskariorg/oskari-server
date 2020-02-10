package org.oskari.maplayer.model;

import java.util.List;

public class ServiceCapabilitiesResultWMS extends ServiceCapabilitiesResult {

    private List<ServiceCapabilitiesResultWMS> structure;

    public List<ServiceCapabilitiesResultWMS> getStructure() {
        return structure;
    }
    public void setStructure(List<ServiceCapabilitiesResultWMS> structure) {
        this.structure = structure;
    }
}

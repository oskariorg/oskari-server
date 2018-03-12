package org.oskari.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;

public class CapabilitiesUpdateResult {

    private final int layerId;
    private final String errorMessage;

    private CapabilitiesUpdateResult(int layerId, String errorMessage) {
        this.layerId = layerId;
        this.errorMessage = errorMessage;
    }

    public static CapabilitiesUpdateResult ok(OskariLayer layer) {
        return new CapabilitiesUpdateResult(layer.getId(), null);
    }

    public static CapabilitiesUpdateResult err(OskariLayer layer, String errorMessage) {
        return new CapabilitiesUpdateResult(layer.getId(), errorMessage);
    }

    public int getLayerId() {
        return layerId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
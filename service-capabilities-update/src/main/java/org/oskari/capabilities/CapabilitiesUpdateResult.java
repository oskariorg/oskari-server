package org.oskari.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;

public class CapabilitiesUpdateResult {

    private final String layerId;
    private final String errorMessage;

    private CapabilitiesUpdateResult(OskariLayer layer, String errorMessage) {
        this.layerId = Integer.toString(layer.getId());
        this.errorMessage = errorMessage;
    }

    public static CapabilitiesUpdateResult ok(OskariLayer layer) {
        return new CapabilitiesUpdateResult(layer, null);
    }

    public static CapabilitiesUpdateResult err(OskariLayer layer, String errorMessage) {
        return new CapabilitiesUpdateResult(layer, errorMessage);
    }

    public String getLayerId() {
        return layerId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
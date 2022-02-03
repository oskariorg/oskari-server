package org.oskari.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;

public class CapabilitiesUpdateResult {

    protected static final String ERR_LAYER_TYPE_UNSUPPORTED = "Layer type not supported for update";
    protected static final String ERR_FAILED_TO_FETCH_CAPABILITIES = "Failed to get Capabilities data";
    protected static final String ERR_LAYER_NOT_FOUND_IN_CAPABILITIES = "Could not find layer from Capabilities";
    protected static final String ERR_FAILED_TO_PARSE_CAPABILITIES = "Failed to parse Capabilities";

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
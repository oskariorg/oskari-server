package org.oskari.capabilities.ogc.wmts;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TileMatrixLink {

    private final TileMatrixSet tms;
    private final List<TileMatrixLimits> limits;

    public TileMatrixLink(@JsonProperty("tileMatrixSet") TileMatrixSet tms,
                          @JsonProperty("limits") List<TileMatrixLimits> limits) {
        this.tms = tms;
        this.limits = limits;
    }

    public TileMatrixSet getTileMatrixSet() {
        return tms;
    }

    public List<TileMatrixLimits> getLimits() {
        return limits;
    }

}

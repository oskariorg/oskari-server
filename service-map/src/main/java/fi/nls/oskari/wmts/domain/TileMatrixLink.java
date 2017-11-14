package fi.nls.oskari.wmts.domain;

import java.util.List;

public class TileMatrixLink {

    private final TileMatrixSet tms;
    private final List<TileMatrixLimits> limits;

    public TileMatrixLink(TileMatrixSet tms, List<TileMatrixLimits> limits) {
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

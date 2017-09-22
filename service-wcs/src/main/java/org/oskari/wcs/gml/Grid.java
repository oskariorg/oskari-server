package org.oskari.wcs.gml;

public class Grid {

    private final GridEnvelope limits;
    private final String[] axes;

    public Grid(GridEnvelope limits, String[] axes) {
        this.limits = limits;
        this.axes = axes;
    }

    public GridEnvelope getLimits() {
        return limits;
    }

    public String[] getAxes() {
        return axes;
    }

}

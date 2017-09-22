package org.oskari.wcs.gml;

public class RectifiedGrid extends Grid {

    private final int dimension;
    private final Point origin;
    private final Point[] offsetVectors;

    public RectifiedGrid(GridEnvelope limits, String[] axes, int dimension, Point origin,
            Point[] offsetVectors) {
        super(limits, axes);
        this.dimension = dimension;
        this.origin = origin;
        this.offsetVectors = offsetVectors;
    }

    public int getDimension() {
        return dimension;
    }

    public Point getOrigin() {
        return origin;
    }

    public Point[] getOffsetVectors() {
        return offsetVectors;
    }

}

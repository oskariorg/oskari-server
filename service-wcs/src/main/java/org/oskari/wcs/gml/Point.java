package org.oskari.wcs.gml;

public class Point {

    private final String srsName;
    private final int dimension;
    private final double[] pos;

    public Point(String srsName, int dimension, double[] pos) {
        this.srsName = srsName;
        this.dimension = dimension;
        this.pos = pos;
    }

    public String getSrsName() {
        return srsName;
    }

    public int getDimension() {
        return dimension;
    }

    public double[] getPos() {
        return pos;
    }

}

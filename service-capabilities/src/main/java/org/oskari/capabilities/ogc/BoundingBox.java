package org.oskari.capabilities.ogc;

public class BoundingBox {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private String srs;

    public BoundingBox(double minX, double maxX,double minY,double maxY, String srs) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.srs = srs;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public String getSrs() {
        return srs;
    }

    public boolean isSrs(String srs) {
        if (srs == null || this.srs == null) {
            return false;
        }
        return srs.equalsIgnoreCase(this.srs);
    }

    public String getWKT() {
        // Duplicated from WKTHelper for convenience and not adding the dependency
        return "POLYGON ((" + getMinX() + " " + getMinY() + ", " +
                getMinX() + " " + getMaxY() + ", " +
                getMaxX() + " " + getMaxY() + ", " +
                getMaxX() + " " + getMinY() + ", " +
                getMinX() + " " + getMinY() + "))";
    }
}
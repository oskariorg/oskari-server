package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BoundingBox {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private String srs;

    public BoundingBox(@JsonProperty("minX") double minX,
                       @JsonProperty("maxX") double maxX,
                       @JsonProperty("minY") double minY,
                       @JsonProperty("maxY") double maxY,
                       @JsonProperty("srs") String srs) {
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
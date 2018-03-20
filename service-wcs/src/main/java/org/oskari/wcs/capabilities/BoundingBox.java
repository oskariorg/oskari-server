package org.oskari.wcs.capabilities;

public class BoundingBox {

    private final String crs;
    private final double lowerCornerLon;
    private final double lowerCornerLat;
    private final double upperCornerLon;
    private final double upperCornerLat;

    public BoundingBox(String crs, double lowerCornerLon, double lowerCornerLat,
            double upperCornerLon, double upperCornerLat) {
        this.crs = crs;
        this.lowerCornerLon = lowerCornerLon;
        this.lowerCornerLat = lowerCornerLat;
        this.upperCornerLon = upperCornerLon;
        this.upperCornerLat = upperCornerLat;
    }

    public String getCrs() {
        return crs;
    }

    public double getLowerCornerLon() {
        return lowerCornerLon;
    }

    public double getLowerCornerLat() {
        return lowerCornerLat;
    }

    public double getUpperCornerLon() {
        return upperCornerLon;
    }

    public double getUpperCornerLat() {
        return upperCornerLat;
    }

}

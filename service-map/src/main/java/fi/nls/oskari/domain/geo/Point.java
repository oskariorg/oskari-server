package fi.nls.oskari.domain.geo;

public class Point {

    private double lat;
    private double lon;

    public Point(final double lat, final double lon) {
        this.lon = lat;
        this.lat = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(final double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(final double lon) {
        this.lon = lon;
    }

    public String toString() {
        return String.valueOf(this.lat + "," + this.lon);
    }

}

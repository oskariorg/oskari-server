package fi.nls.oskari.domain.geo;


public class Point {

    private double lat;
    private double lon;
    /*
      lat is to the north   (y)
      lon is to the east    (x)
     */
    public Point(final double lon, final double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLat() {
        return lat;
    }
    public String getLatToString() {
        return Double.toString(this.lat);
    }

    public void setLat(final double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }
    public String getLonToString() {
        return Double.toString(this.lon);
    }

    public void setLon(final double lon) {
        this.lon = lon;
    }

    public String toString() {
        return String.valueOf(this.lon + "," + this.lat);
    }
    public void switchLonLat() {
        double dmp = this.lon;
        this.lon = this.lat;
        this.lat = dmp;
    }

}

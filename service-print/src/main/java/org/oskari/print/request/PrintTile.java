package org.oskari.print.request;

public class PrintTile {

    private final double[] bbox;
    private final String url;

    public PrintTile(double[] bbox, String url) {
        this.bbox = bbox;
        this.url = url;
    }

    public double[] getBbox() {
        return bbox;
    }

    public String getURL() {
        return url;
    }

}

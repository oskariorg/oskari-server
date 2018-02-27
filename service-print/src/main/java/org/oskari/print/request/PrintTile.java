package org.oskari.print.request;

public class PrintTile {

    private final double[] bbox;
    private final String url;

    public PrintTile(double[] bbox, String url) {
        if (bbox == null || bbox.length != 4) {
            throw new IllegalArgumentException("bbox must be non-null and contain 4 elements");
        }
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("url must be non-null and non-empty");
        }
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

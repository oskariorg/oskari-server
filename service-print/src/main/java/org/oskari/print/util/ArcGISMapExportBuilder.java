package org.oskari.print.util;

import java.util.LinkedHashMap;
import java.util.Map;

import fi.nls.oskari.util.IOHelper;

/**
 * ArcGIS REST API Export Map Request (KVP) Builder
 */
public class ArcGISMapExportBuilder {

    private String endPoint;
    private String layer;
    private double[] bbox;
    private int bboxsr;
    private int width;
    private int height;
    private boolean transparent;

    public ArcGISMapExportBuilder endPoint(String endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public ArcGISMapExportBuilder layer(String layer) {
        this.layer = layer;
        return this;
    }

    public ArcGISMapExportBuilder bbox(double[] bbox) throws IllegalArgumentException {
        if (bbox == null || bbox.length != 4) {
            throw new IllegalArgumentException("bbox length must be 4");
        }
        this.bbox = bbox;
        return this;
    }

    public ArcGISMapExportBuilder crs(String srs) {
        int i = srs.lastIndexOf(':');
        if (i < 0) {
            throw new IllegalArgumentException("srs must contain :");
        }
        return srid(Integer.parseInt(srs.substring(i + 1)));
    }

    public ArcGISMapExportBuilder srid(int srid) {
        this.bboxsr = srid;
        return this;
    }

    public ArcGISMapExportBuilder width(int width) {
        this.width = width;
        return this;
    }

    public ArcGISMapExportBuilder height(int height) {
        this.height = height;
        return this;
    }

    public ArcGISMapExportBuilder transparent(boolean transparent) {
        this.transparent = transparent;
        return this;
    }

    public String build() throws IllegalArgumentException {
        if (endPoint == null || endPoint.length() == 0) {
            throw new IllegalArgumentException("Required parameter 'endPoint' missing");
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("F", "IMAGE");
        params.put("FORMAT", "PNG32");
        params.put("BBOX", String.format("%f,%f,%f,%f", bbox[0], bbox[1], bbox[2], bbox[3]));
        params.put("BBOXSR", Integer.toString(bboxsr));
        params.put("SIZE", String.format("%d,%d", width, height));
        params.put("LAYER", String.format("show:%s", layer));
        params.put("TRANSPARENT", transparent ? "TRUE" : "FALSE");
        params.put("DPI", Integer.toString(90));

        return IOHelper.constructUrl(endPoint, params);
    }

}

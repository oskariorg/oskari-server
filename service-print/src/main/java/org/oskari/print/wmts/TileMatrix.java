package org.oskari.print.wmts;

import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

public class TileMatrix {

    private String id;
    private double scaleDenominator;
    private double[] topLeftCorner = {-1, -1};
    private int tileWidth = 256;
    private int tileHeight = 256;
    private int matrixWidth;
    private int matrixHeight;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getScaleDenominator() {
        return scaleDenominator;
    }

    public void setScaleDenominator(double scaleDenominator) {
        this.scaleDenominator = scaleDenominator;
    }

    public double[] getTopLeftCorner() {
        return topLeftCorner;
    }

    public void setTopLeftCorner(double first, double second) {
        topLeftCorner[0] = first;
        topLeftCorner[1] = second;
    }

    public void setTopLeftCorner(String topLeftCorner) {
        if(topLeftCorner == null) {
            return;
        }
        final String[] split = topLeftCorner.split("\\s+");
        if(split.length != 2) {
            return;
        }
        setTopLeftCorner(ConversionHelper.getDouble(split[0], -1),
                ConversionHelper.getDouble(split[1], -1));
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public void setMatrixWidth(int matrixWidth) {
        this.matrixWidth = matrixWidth;
    }

    public int getMatrixHeight() {
        return matrixHeight;
    }

    public void setMatrixHeight(int matrixHeight) {
        this.matrixHeight = matrixHeight;
    }

    public JSONObject getAsJSON() {
        /*
        {
            "supportedCRS": "urn:ogc:def:crs:EPSG:6.3:3067",
            "identifier": "0",
            "scaleDenominator": 29257142.85714286,
            "topLeftCorner": {
                "lon": -548576,
                "lat": 8388608
            },
            "tileWidth": 256,
            "tileHeight": 256,
            "matrixWidth": 1,
            "matrixHeight": 1
        }
        */
        JSONObject obj = new JSONObject();
        JSONHelper.putValue(obj, "identifier", getId());
        JSONHelper.putValue(obj, "scaleDenominator", getScaleDenominator());

        JSONObject topLeft = new JSONObject();
        JSONHelper.putValue(topLeft, "lon", getTopLeftCorner()[0]);
        JSONHelper.putValue(topLeft, "lat", getTopLeftCorner()[1]);
        JSONHelper.putValue(obj, "topLeftCorner", topLeft);

        JSONHelper.putValue(obj, "tileWidth", getTileWidth());
        JSONHelper.putValue(obj, "tileHeight", getTileHeight());
        JSONHelper.putValue(obj, "matrixWidth", getMatrixWidth());
        JSONHelper.putValue(obj, "matrixHeight", getMatrixHeight());
        return obj;
    }
}

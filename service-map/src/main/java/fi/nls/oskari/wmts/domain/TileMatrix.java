package fi.nls.oskari.wmts.domain;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * @see http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd
 */
public class TileMatrix {

    private final String id;
    private final double scaleDenominator;
    private final double[] topLeftCorner;
    private final int tileWidth;
    private final int tileHeight;
    private final int matrixWidth;
    private final int matrixHeight;

    public TileMatrix(String id, double scaleDenominator, double[] topLeftCorner,
            int tileWidth, int tileHeight, int matrixWidth, int matrixHeight) {
        this.id = id;
        this.scaleDenominator = scaleDenominator;
        this.topLeftCorner = topLeftCorner;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.matrixWidth = matrixWidth;
        this.matrixHeight = matrixHeight;
        validate();
    }

    private void validate() throws IllegalArgumentException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Missing id");
        }
        if (scaleDenominator < 0) {
            throw new IllegalArgumentException("ScaleDenominator must be non-negative");
        }
        if (topLeftCorner == null || topLeftCorner.length != 2) {
            throw new IllegalArgumentException("TopLeftCorner must exist and must have two points");
        }
        if (tileWidth < 1) {
            throw new IllegalArgumentException("TileWidth must be positive");
        }
        if (tileHeight < 1) {
            throw new IllegalArgumentException("TileHeight must be positive");
        }
        if (matrixWidth < 1) {
            throw new IllegalArgumentException("MatrixWidth must be positive");
        }
        if (matrixHeight < 1) {
            throw new IllegalArgumentException("MatrixHeight must be positive");
        }
    }

    public String getId() {
        return id;
    }

    public double getScaleDenominator() {
        return scaleDenominator;
    }

    public double[] getTopLeftCorner() {
        return topLeftCorner;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public int getMatrixHeight() {
        return matrixHeight;
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

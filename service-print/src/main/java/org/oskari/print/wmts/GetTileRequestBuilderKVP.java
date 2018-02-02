package org.oskari.print.wmts;

/**
 * WMTS GetTile Request Builder (KVP)
 */
public class GetTileRequestBuilderKVP implements GetTileRequestBuilder {

    private String endPoint;
    private String layer;
    private String style;
    private String format;
    private String tileMatrixSet;
    private String tileMatrix;
    private int tileRow = -1;
    private int tileCol = -1;

    public GetTileRequestBuilderKVP endPoint(String endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public GetTileRequestBuilderKVP format(String format) {
        this.format = format;
        return this;
    }

    public GetTileRequestBuilderKVP layer(String layer) {
        this.layer = layer;
        return this;
    }

    public GetTileRequestBuilderKVP style(String style) {
        this.style = style;
        return this;
    }

    public GetTileRequestBuilderKVP tileMatrixSet(String tileMatrixSet) {
        this.tileMatrixSet = tileMatrixSet;
        return this;
    }

    public GetTileRequestBuilderKVP tileMatrix(String tileMatrix) {
        this.tileMatrix = tileMatrix;
        return this;
    }

    public GetTileRequestBuilderKVP tileRow(int tileRow) {
        this.tileRow = tileRow;
        return this;
    }

    public GetTileRequestBuilderKVP tileCol(int tileCol) {
        this.tileCol = tileCol;
        return this;
    }

    public String build() throws IllegalArgumentException {
        if (endPoint == null || endPoint.length() == 0) {
            throw new IllegalArgumentException("Required parameter 'endPoint' missing");
        }
        if (layer == null || layer.length() == 0) {
            throw new IllegalArgumentException("Required parameter 'layer' missing");
        }
        if (style == null || style.length() == 0) {
            throw new IllegalArgumentException("Required parameter 'style' missing");
        }
        if (format == null || format.length() == 0) {
            throw new IllegalArgumentException("Required parameter 'format' missing");
        }
        if (tileMatrixSet == null || tileMatrixSet.length() == 0) {
            throw new IllegalArgumentException("Required parameter 'tileMatrixSet' missing");
        }
        if (tileMatrix == null || tileMatrix.length() == 0) {
            throw new IllegalArgumentException("Required parameter 'tileMatrix' missing");
        }
        if (tileRow < 0) {
            throw new IllegalArgumentException("Required parameter 'tileRow' missing");
        }
        if (tileCol < 0) {
            throw new IllegalArgumentException("Required parameter 'tileCol' missing");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(endPoint);
        sb.append("?SERVICE=WMTS");
        sb.append("&REQUEST=GetTile");
        sb.append("&VERSION=1.0.0");
        sb.append("&LAYER=").append(layer);
        sb.append("&STYLE=").append(style);
        sb.append("&FORMAT=").append(format);
        sb.append("&TILEMATRIXSET=").append(tileMatrixSet);
        sb.append("&TILEMATRIX=").append(tileMatrix);
        sb.append("&TILEROW=").append(tileRow);
        sb.append("&TILECOL=").append(tileCol);
        return sb.toString();
    }

}

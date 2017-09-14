package fi.nls.oskari.wmts.domain;

/**
 * @see http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd
 */
public class TileMatrixLimits {

    private final TileMatrix tm;
    private final int minTileRow;
    private final int maxTileRow;
    private final int minTileCol;
    private final int maxTileCol;

    public TileMatrixLimits(TileMatrix tm, int minTileRow, int maxTileRow,
            int minTileCol, int maxTileCol) throws IllegalArgumentException {
        this.tm = tm;
        this.minTileRow = minTileRow;
        this.maxTileRow = maxTileRow;
        this.minTileCol = minTileCol;
        this.maxTileCol = maxTileCol;
        validate();
    }

    private void validate() throws IllegalArgumentException {
        if (minTileRow < 0) {
            throw new IllegalArgumentException("Negative MinTileRow");
        }
        if (maxTileRow >= tm.getMatrixWidth()) {
            throw new IllegalArgumentException("MaxTileRow must be < MatrixWidth");
        }
        if (minTileRow > maxTileRow) {
            throw new IllegalArgumentException("MinTileRow must be < MaxTileRow");
        }
        if (minTileCol < 0) {
            throw new IllegalArgumentException("Negative MinTileCol");
        }
        if (maxTileCol >= tm.getMatrixHeight()) {
            throw new IllegalArgumentException("MaxTileCol must be < MatrixHeight");
        }
        if (minTileCol > maxTileCol) {
            throw new IllegalArgumentException("MinTileCol must be < MaxTileCol");
        }
    }

    public TileMatrix getTileMatrix() {
        return tm;
    }

    public int getMinTileRow() {
        return minTileRow;
    }

    public int getMaxTileRow() {
        return maxTileRow;
    }

    public int getMinTileCol() {
        return minTileCol;
    }

    public int getMaxTileCol() {
        return maxTileCol;
    }

}

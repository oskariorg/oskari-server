package fi.nls.oskari.wmts.domain;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * @see http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd
 */
public class TileMatrixLimits {

    private static Logger LOG = LogFactory.getLogger(TileMatrixLimits.class);

    private final TileMatrix tm;
    private final int minTileRow;
    private final int maxTileRow;
    private final int minTileCol;
    private final int maxTileCol;

    public TileMatrixLimits(TileMatrix tm, int minTileRow, int maxTileRow,
            int minTileCol, int maxTileCol) {
        this.tm = tm;
        this.minTileRow = minTileRow;
        this.maxTileRow = maxTileRow;
        this.minTileCol = minTileCol;
        this.maxTileCol = maxTileCol;
        validate();
    }

    /**
     * Just log as warnings instead of throwing exceptions for now
     * TODO: Don't accept invalid values
     */
    private void validate() {
        if (minTileRow < 0) {
            LOG.warn(tm.getId(), "Negative MinTileRow");
        }
        if (maxTileRow >= tm.getMatrixWidth()) {
            LOG.warn(tm.getId(), "MaxTileRow must be < MatrixWidth");
        }
        if (minTileRow > maxTileRow) {
            LOG.warn(tm.getId(), "MinTileRow must be < MaxTileRow");
        }
        if (minTileCol < 0) {
            LOG.warn(tm.getId(), "Negative MinTileCol");
        }
        if (maxTileCol >= tm.getMatrixHeight()) {
            LOG.warn(tm.getId(), "MaxTileCol must be < MatrixHeight");
        }
        if (minTileCol > maxTileCol) {
            LOG.warn(tm.getId(), "MinTileCol must be < MaxTileCol");
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

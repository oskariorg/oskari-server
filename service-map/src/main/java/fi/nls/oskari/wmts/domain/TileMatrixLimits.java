package fi.nls.oskari.wmts.domain;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * Metadata describing the limits of a TileMatrix for a layer
 *
 * The schema and the specification are obviously erroneous - use common sense when validating
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
     * TODO: Throw exceptions instead of accepting invalid values
     */
    private void validate() {
        if (minTileRow < 0) {
            LOG.warn(tm.getId(), "MinTileRow must be nonNegative");
        }
        // schema: positiveInteger
        // But probably should be nonNegativeInteger
        if (maxTileRow < 0) {
            LOG.warn(tm.getId(), "MaxTileRow must be nonNegative");
        }
        if (minTileRow > maxTileRow) {
            LOG.warn(tm.getId(), "MinTileRow must be <= MaxTileRow");
        }
        // schema: 'Maximim tile row index valid for this layer. From minTileRow to matrixWidth-1 ...'
        // Should be from minTileRow to matrixHeight-1
        if (maxTileRow >= tm.getMatrixHeight()) {
            LOG.warn(tm.getId(), "MaxTileRow must be < MatrixHeight");
        }

        if (minTileCol < 0) {
            LOG.warn(tm.getId(), "MinTileCol must be nonNegative");
        }
        // schema: positiveInteger
        // But probably should be nonNegativeInteger
        if (maxTileCol < 0) {
            LOG.warn(tm.getId(), "MaxTileCol must be nonNegative");
        }
        if (minTileCol > maxTileCol) {
            LOG.warn(tm.getId(), "MinTileCol must be <= MaxTileCol");
        }
        // schema: 'Maximim tile column index valid for this layer. From minTileCol to tileHeight-1 ...'
        // Should be from minTileCol to matrixWidth-1
        if (maxTileCol >= tm.getMatrixWidth()) {
            LOG.warn(tm.getId(), "MaxTileCol must be < MatrixWidth");
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

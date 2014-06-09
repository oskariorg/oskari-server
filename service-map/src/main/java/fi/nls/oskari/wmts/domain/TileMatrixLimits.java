package fi.nls.oskari.wmts.domain;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 6.6.2014
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
public class TileMatrixLimits {

    private String tileMatrix;
    private int minTileRow;
    private int maxTileRow;
    private int minTileCol;
    private int maxTileCol;

    public String getTileMatrix() {
        return tileMatrix;
    }

    public void setTileMatrix(String tileMatrix) {
        this.tileMatrix = tileMatrix;
    }

    public int getMinTileRow() {
        return minTileRow;
    }

    public void setMinTileRow(int minTileRow) {
        this.minTileRow = minTileRow;
    }

    public int getMaxTileRow() {
        return maxTileRow;
    }

    public void setMaxTileRow(int maxTileRow) {
        this.maxTileRow = maxTileRow;
    }

    public int getMinTileCol() {
        return minTileCol;
    }

    public void setMinTileCol(int minTileCol) {
        this.minTileCol = minTileCol;
    }

    public int getMaxTileCol() {
        return maxTileCol;
    }

    public void setMaxTileCol(int maxTileCol) {
        this.maxTileCol = maxTileCol;
    }
}

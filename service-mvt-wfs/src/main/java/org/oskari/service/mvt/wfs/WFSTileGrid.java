package org.oskari.service.mvt.wfs;

public class WFSTileGrid {

    public static final int TILE_SIZE = 256;

    private final double originX;
    private final double originY;
    private final double[] resolutions;

    public WFSTileGrid(double[] extent, int maxZoom) {
        double w = extent[2] - extent[0];
        double h = extent[3] - extent[1];
        if (w != h) {
            throw new IllegalArgumentException("Only square extents are supported");
        }

        this.originX = extent[0];
        this.originY = extent[3];

        this.resolutions = new double[maxZoom + 1];

        resolutions[0] = w / TILE_SIZE;
        for (int i = 1; i < maxZoom; i++) {
            resolutions[i] = resolutions[i - 1] / 2;
        }
    }

    public int getMaxZoom() {
        return resolutions.length - 1;
    }

    public double[] getTileExtent(TileCoord tile) {
        double tileSizeInNature = TILE_SIZE * resolutions[tile.getZ()];
        double x1 = originX + tile.getX() * tileSizeInNature;
        double y1 = originY - tile.getY() * tileSizeInNature;
        return new double[] { x1, y1 - tileSizeInNature, x1 + tileSizeInNature, y1 };
    }

    public static int getMatrixSize(int z) {
        return 1 << z; // 2^z
    }

    public int getZForResolution(int resolution, int direction) {
        if (resolutions[0] <= resolution) {
            return 0;
        }

        int maxZoom = getMaxZoom();
        for (int z = 1; z < maxZoom; z++) {
            if (resolutions[z] == resolution) {
                return z;
            }
            if (resolutions[z] < resolution) {
                switch (direction) {
                case -1:
                    return z - 1;
                case  0:
                    double dcurr = Math.abs(resolutions[z - 1] - resolution);
                    double dnext = Math.abs(resolutions[z] - resolution);
                    return dcurr < dnext ? z : z + 1;
                case  1:
                    return z;
                }
            }
        }
        return maxZoom;
    }

    public double getResolutionForZ(int z) {
        return resolutions[z];
    }

}

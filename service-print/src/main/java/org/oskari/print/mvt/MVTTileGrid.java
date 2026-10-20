package org.oskari.print.mvt;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.service.mvt.TileCoord;

import fi.nls.oskari.domain.map.OskariLayer;

/**
 * Tile grid for a vector tile layer.
 *
 * The layer's own options are used when present, otherwise the grid is derived
 * from the projection extent the same way {@link org.oskari.service.mvt.WFSTileGrid}
 * does. Unlike WFSTileGrid a non-square extent doesn't throw; the resolutions are
 * derived from the larger dimension.
 */
public class MVTTileGrid {

    private static final int DEFAULT_TILE_SIZE = 256;
    private static final int DEFAULT_MAX_ZOOM = 20;
    // Without a tile grid of its own OpenLayers' VectorTile source covers the
    // projection extent with 512px tiles down to zoom 22
    private static final int SOURCE_TILE_SIZE = 512;
    private static final int SOURCE_MAX_ZOOM = 22;

    private final double originX;
    private final double originY;
    private final double maxX;
    private final double minY;
    private final int tileSize;
    private final double[] resolutions;

    MVTTileGrid(double[] extent, int maxZoom, int tileSize) {
        this(extent, tileSize, derive(extent, maxZoom, tileSize));
    }

    MVTTileGrid(double[] extent, int tileSize, double[] resolutions) {
        this.originX = extent[0];
        this.originY = extent[3];
        this.maxX = extent[2];
        this.minY = extent[1];
        this.tileSize = tileSize;
        this.resolutions = resolutions;
    }

    /**
     * The resolutions of a grid that halves each level, used when the layer
     * doesn't list them itself.
     */
    private static double[] derive(double[] extent, int maxZoom, int tileSize) {
        double width = extent[2] - extent[0];
        double height = extent[3] - extent[1];
        // Zoom level 0 holds the whole extent, the larger dimension decides the resolution
        double size = Math.max(width, height);

        double[] resolutions = new double[maxZoom + 1];
        resolutions[0] = size / tileSize;
        for (int i = 1; i <= maxZoom; i++) {
            resolutions[i] = resolutions[i - 1] / 2;
        }
        return resolutions;
    }

    /**
     * Reads the tile grid from the layer's options, falling back to the given
     * projection extent.
     * @return tile grid or null when no extent is known for the projection
     */
    public static MVTTileGrid create(OskariLayer layer, double[] fallbackExtent) {
        JSONObject options = layer.getOptions();
        JSONObject tileGrid = options != null ? options.optJSONObject("tileGrid") : null;

        double[] extent = tileGrid != null ? readExtent(tileGrid.optJSONArray("extent")) : null;
        if (extent == null) {
            extent = fallbackExtent;
        }
        if (extent == null) {
            return null;
        }
        if (tileGrid == null) {
            return new MVTTileGrid(extent, SOURCE_MAX_ZOOM, SOURCE_TILE_SIZE);
        }

        int tileSize = readTileSize(tileGrid);

        // Listed resolutions are authoritative: a level not listed has no tiles
        // and the service errors on it.
        double[] resolutions = readResolutions(tileGrid.optJSONArray("resolutions"));
        if (resolutions != null) {
            return new MVTTileGrid(extent, tileSize, resolutions);
        }

        int maxZoom = tileGrid.optInt("maxZoom", DEFAULT_MAX_ZOOM);
        return new MVTTileGrid(extent, maxZoom, tileSize);
    }

    /**
     * OpenLayers takes tileSize as either a number or a [width, height] pair,
     * and a layer's options carry whichever the grid was written with.
     */
    private static int readTileSize(JSONObject tileGrid) {
        JSONArray pair = tileGrid.optJSONArray("tileSize");
        if (pair != null && pair.length() > 0) {
            int width = pair.optInt(0, DEFAULT_TILE_SIZE);
            return width > 0 ? width : DEFAULT_TILE_SIZE;
        }
        int size = tileGrid.optInt("tileSize", DEFAULT_TILE_SIZE);
        return size > 0 ? size : DEFAULT_TILE_SIZE;
    }

    /**
     * @return the resolutions the layer lists, null when it lists none or they
     *         are not all numbers
     */
    private static double[] readResolutions(JSONArray listed) {
        if (listed == null || listed.length() == 0) {
            return null;
        }
        double[] resolutions = new double[listed.length()];
        for (int i = 0; i < resolutions.length; i++) {
            resolutions[i] = listed.optDouble(i, Double.NaN);
            if (!(resolutions[i] > 0)) {
                return null;
            }
        }
        return resolutions;
    }

    private static double[] readExtent(JSONArray extent) {
        if (extent == null || extent.length() != 4) {
            return null;
        }
        double[] values = new double[4];
        for (int i = 0; i < 4; i++) {
            values[i] = extent.optDouble(i, Double.NaN);
            if (Double.isNaN(values[i])) {
                return null;
            }
        }
        return values;
    }

    /**
     * @return the zoom level whose resolution is closest to the requested one
     */
    public int getClosestZoom(double resolution) {
        int closest = 0;
        double smallestDiff = Double.MAX_VALUE;
        for (int z = 0; z < resolutions.length; z++) {
            double diff = Math.abs(resolutions[z] - resolution);
            if (diff < smallestDiff) {
                smallestDiff = diff;
                closest = z;
            }
        }
        return closest;
    }

    /**
     * The zoom a resolution stands for, as a fraction and without stopping at
     * the grid's last level. A Mapbox style is read at the zoom the map is
     * viewed at, which is this one, while the tiles come from the level the
     * grid actually has.
     */
    public double getZoomForResolution(double resolution) {
        if (resolution <= 0 || resolutions.length == 0) {
            return 0;
        }
        // The levels are a geometric series, so the zoom is a logarithm of it
        double factor = resolutions.length > 1 ? resolutions[0] / resolutions[1] : 2;
        if (!(factor > 1)) {
            return getClosestZoom(resolution);
        }
        return Math.max(0, Math.log(resolutions[0] / resolution) / Math.log(factor));
    }

    public double[] getTileExtent(TileCoord tile) {
        double tileSpan = tileSize * resolutions[tile.getZ()];
        // Each edge from its own index, so that the neighbours sharing it get
        // the same value rather than one that differs in the last bit
        return new double[] {
                originX + tile.getX() * tileSpan,
                originY - (tile.getY() + 1) * tileSpan,
                originX + (tile.getX() + 1) * tileSpan,
                originY - tile.getY() * tileSpan };
    }

    /**
     * @return tiles of the given zoom level covering the bbox,
     *         tiles outside the grid are left out
     */
    public List<TileCoord> getTiles(double[] bbox, int z) {
        double tileSpan = tileSize * resolutions[z];
        int lastCol = (int) Math.ceil((maxX - originX) / tileSpan) - 1;
        int lastRow = (int) Math.ceil((originY - minY) / tileSpan) - 1;

        int minCol = (int) Math.floor((bbox[0] - originX) / tileSpan);
        int maxCol = (int) Math.ceil((bbox[2] - originX) / tileSpan) - 1;
        int minRow = (int) Math.floor((originY - bbox[3]) / tileSpan);
        int maxRow = (int) Math.ceil((originY - bbox[1]) / tileSpan) - 1;

        minCol = Math.max(0, minCol);
        minRow = Math.max(0, minRow);
        maxCol = Math.min(lastCol, maxCol);
        maxRow = Math.min(lastRow, maxRow);

        List<TileCoord> tiles = new ArrayList<>();
        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                tiles.add(new TileCoord(z, col, row));
            }
        }
        return tiles;
    }
}

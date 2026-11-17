package org.oskari.print.mvt;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.service.mvt.TileCoord;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;

public class MVTTileGridTest {

    private static final double DELTA = 0.0001;

    // 256 units wide extent => resolution 1 at zoom 0
    private static final double[] EXTENT = { 0, 0, 256, 256 };

    private static MVTTileGrid grid() {
        return new MVTTileGrid(EXTENT, 4, 256);
    }

    @Test
    public void tileExtentIsMeasuredFromTopLeftCorner() {
        MVTTileGrid grid = grid();
        double[] topLeft = grid.getTileExtent(new TileCoord(1, 0, 0));
        Assertions.assertArrayEquals(new double[] { 0, 128, 128, 256 }, topLeft, DELTA,
                "first tile is in the top left corner");

        double[] bottomRight = grid.getTileExtent(new TileCoord(1, 1, 1));
        Assertions.assertArrayEquals(new double[] { 128, 0, 256, 128 }, bottomRight, DELTA,
                "tile rows grow downwards");
    }

    @Test
    public void tilesOutsideTheGridAreNotRequested() {
        MVTTileGrid grid = grid();
        List<TileCoord> tiles = grid.getTiles(new double[] { -100, -100, 10, 10 }, 1);
        Assertions.assertEquals(1, tiles.size(), "only the tile inside the grid");
        Assertions.assertEquals(0, tiles.get(0).getX(), "column");
        Assertions.assertEquals(1, tiles.get(0).getY(), "bottom row");
    }

    @Test
    public void tilesAreCountedFromTheExtentNotTheZoom() {
        // Listed resolutions starting at half of extent / tileSize
        MVTTileGrid grid = MVTTileGrid.create(
                layerWith("{\"extent\": [0, 0, 512, 512], \"resolutions\": [1, 0.5]}"), null);
        List<TileCoord> tiles = grid.getTiles(new double[] { -100, -100, 600, 600 }, 0);
        Assertions.assertEquals(4, tiles.size(), "level 0 has 2x2 tiles");
        Assertions.assertEquals(1, tiles.get(3).getX(), "last column");
        Assertions.assertEquals(1, tiles.get(3).getY(), "last row");
    }

    @Test
    public void aNonSquareExtentHasFewerRowsThanColumns() {
        MVTTileGrid grid = new MVTTileGrid(new double[] { 0, 0, 512, 256 }, 4, 256);
        List<TileCoord> tiles = grid.getTiles(new double[] { 0, -512, 512, 512 }, 1);
        Assertions.assertEquals(2, tiles.size(), "one row of two tiles at zoom 1");
    }

    /** The tile grid an Oskari vector tile layer carries in its options */
    private static OskariLayer layerWith(String tileGrid) {
        OskariLayer layer = new OskariLayer();
        layer.setOptions(JSONHelper.createJSONObject("{\"tileGrid\": " + tileGrid + "}"));
        return layer;
    }

    @Test
    public void withoutATileGridTheExtentIsTiledIn512pxTiles() {
        // The browser's VectorTile source defaults to 512px tiles, a 256px
        // default would read the style one zoom off from what the map shows
        MVTTileGrid grid = MVTTileGrid.create(new OskariLayer(), new double[] { 0, 0, 1024, 1024 });
        Assertions.assertArrayEquals(new double[] { 0, 0, 1024, 1024 },
                grid.getTileExtent(new TileCoord(0, 0, 0)), DELTA, "extent over 512px");
    }

    @Test
    public void tileSizeIsReadFromAPair() {
        // OpenLayers takes tileSize as a number or a [width, height] pair
        MVTTileGrid grid = MVTTileGrid.create(
                layerWith("{\"extent\": [0, 0, 512, 512], \"tileSize\": [512, 512]}"), null);
        List<TileCoord> tiles = grid.getTiles(new double[] { 0, 0, 512, 512 }, 0);
        Assertions.assertEquals(1, tiles.size(), "one 512px tile covers the extent");
    }

    @Test
    public void theViewsZoomIsNotStoppedAtTheGridsLastLevel() {
        // The tiles stop at the last resolution, the style does not: a view
        // finer than the grid is drawn by scaling those tiles up, and the
        // style still has to be read at the zoom the reader sees
        MVTTileGrid grid = MVTTileGrid.create(
                layerWith("{\"extent\": [0, 0, 256, 256], \"resolutions\": [4, 2, 1]}"), null);
        Assertions.assertEquals(2, grid.getClosestZoom(0.25), "tiles stop at the last level");
        Assertions.assertEquals(4, grid.getZoomForResolution(0.25), DELTA, "the view does not");
        Assertions.assertEquals(0, grid.getZoomForResolution(4), DELTA, "level 0");
        Assertions.assertEquals(1, grid.getZoomForResolution(2), DELTA, "level 1");
        Assertions.assertEquals(0.5, grid.getZoomForResolution(Math.sqrt(8)), DELTA, "in between");
    }
}

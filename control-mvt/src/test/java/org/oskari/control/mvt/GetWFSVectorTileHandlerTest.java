package org.oskari.control.mvt;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.service.mvt.TileCoord;

public class GetWFSVectorTileHandlerTest {

    @Test
    public void testGetTilesToLoad() {
        int targetZ = 5;

        int z = 5;
        int x = 16;
        int y = 17;

        // Expect that we load the tile and the tiles around it
        // Which should be
        // 15<=x<=17
        // 16<=y<=18

        int minExpectedX = 15;
        int minExpectedY = 16;
        int expectedMatrixLen = 3;
        int expectedLen = expectedMatrixLen * expectedMatrixLen;

        List<TileCoord> tiles = GetWFSVectorTileHandler.getTilesToLoad(targetZ, z, x, y);
        for (TileCoord tile : tiles) {
            Assertions.assertEquals(targetZ, tile.getZ());
        }
        Assertions.assertEquals(expectedLen, tiles.size());
        Collections.sort(tiles, Comparator.comparing(TileCoord::getX).thenComparing(TileCoord::getY));

        int index = 0;
        for (int i = 0; i < expectedMatrixLen; i++) {
            int expectedX = minExpectedX + i;
            for (int j = 0; j < expectedMatrixLen; j++) {
                int expectedY = minExpectedY + j;
                TileCoord tile = tiles.get(index++);
                Assertions.assertEquals(expectedX, tile.getX());
                Assertions.assertEquals(expectedY, tile.getY());
            }
        }
    }

    @Test
    public void testGetTilesToLoadLowerZoom() {
        int targetZ = 5;

        int z = 4;
        int x = 10;
        int y = 20;

        // Expect that we load all the tiles inside (z=4, x=10, y=20)
        // Which should be (in z=5):
        // - (x=20, y=40)
        // - (x=20, y=41)
        // - (x=21, y=40)
        // - (x=21, y=41)

        // Then adding the buffer we should get
        // 19<=x<=22
        // 39<=y<=42

        int minExpectedX = 19;
        int minExpectedY = 39;
        int expectedMatrixLen = 4;
        int expectedLen = expectedMatrixLen * expectedMatrixLen;

        List<TileCoord> tiles = GetWFSVectorTileHandler.getTilesToLoad(targetZ, z, x, y);
        for (TileCoord tile : tiles) {
            Assertions.assertEquals(targetZ, tile.getZ());
        }
        Assertions.assertEquals(expectedLen, tiles.size());
        Collections.sort(tiles, Comparator.comparing(TileCoord::getX).thenComparing(TileCoord::getY));

        int index = 0;
        for (int i = 0; i < expectedMatrixLen; i++) {
            int expectedX = minExpectedX + i;
            for (int j = 0; j < expectedMatrixLen; j++) {
                int expectedY = minExpectedY + j;
                TileCoord tile = tiles.get(index++);
                Assertions.assertEquals(expectedX, tile.getX());
                Assertions.assertEquals(expectedY, tile.getY());
            }
        }
    }

    @Test
    public void testGetTilesToLoadHigherZoom() {
        int targetZ = 5;

        int z = 6;
        int x = 62;
        int y = 35;

        // Expect that we load the tile in which this tile belongs to
        // which is x = 62/2 = 31, y = 35/2 = 17 (rounding down)
        // And then the tiles around that for buffer bringing us to:
        // 30<=x<=32
        // 16<=y<=18
        int minExpectedX = 30;
        int minExpectedY = 16;
        int expectedMatrixLen = 3;
        int expectedLen = expectedMatrixLen * expectedMatrixLen;

        List<TileCoord> tiles = GetWFSVectorTileHandler.getTilesToLoad(targetZ, z, x, y);
        for (TileCoord tile : tiles) {
            Assertions.assertEquals(targetZ, tile.getZ());
        }
        Assertions.assertEquals(expectedLen, tiles.size());
        Collections.sort(tiles, Comparator.comparing(TileCoord::getX).thenComparing(TileCoord::getY));

        int index = 0;
        for (int i = 0; i < expectedMatrixLen; i++) {
            int expectedX = minExpectedX + i;
            for (int j = 0; j < expectedMatrixLen; j++) {
                int expectedY = minExpectedY + j;
                TileCoord tile = tiles.get(index++);
                Assertions.assertEquals(expectedX, tile.getX());
                Assertions.assertEquals(expectedY, tile.getY());
            }
        }
    }

}

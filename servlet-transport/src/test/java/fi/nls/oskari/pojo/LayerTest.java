package fi.nls.oskari.pojo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class LayerTest {

    @Test
    public void testIsTile() {
        Layer layer = new Layer();
        List<List<Double>> tiles = new ArrayList<List<Double>>();
        List<Double> tile = new ArrayList<Double>();
        tile.add(1.0);
        tile.add(2.0);
        tile.add(3.0);
        tile.add(4.0);

        List<Double> tile2 = new ArrayList<Double>();
        tile2.add(1.0);
        tile2.add(2.0);
        tile2.add(4.0);
        tile2.add(8.0);

        List<Double> tile3 = new ArrayList<Double>();
        tile3.add(1.0);
        tile3.add(5.0);
        tile3.add(4.0);
        tile3.add(8.0);


        tiles.add(tile);
        tiles.add(tile2);
        layer.setTiles(tiles);
        assertTrue("should find", layer.isTile(tile));
        assertTrue("should not find", layer.isTile(tile3) == false);
    }

}

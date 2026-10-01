package org.oskari.print.mvt;

import java.util.List;

import no.ecc.vectortile.VectorTileDecoder;

/**
 * A decoded vector tile: features in tile local coordinates together with the
 * projected extent needed to place them on the map.
 */
public class MVTTile {

    private final double[] extent;
    private final List<VectorTileDecoder.Feature> features;

    public MVTTile(double[] extent, List<VectorTileDecoder.Feature> features) {
        this.extent = extent;
        this.features = features;
    }

    /**
     * @return tile extent in the projection of the print: minX, minY, maxX, maxY
     */
    public double[] getExtent() {
        return extent;
    }

    public List<VectorTileDecoder.Feature> getFeatures() {
        return features;
    }
}

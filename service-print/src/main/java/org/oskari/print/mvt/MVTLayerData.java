package org.oskari.print.mvt;

import java.util.Collections;
import java.util.List;

/**
 * What a vector tile layer needs to be drawn: the tiles covering the print,
 * the style layers resolved at the printed zoom and the glyphs and icons those
 * style layers use. Everything here is loaded before drawing starts.
 */
public class MVTLayerData {

    private final List<MVTTile> tiles;
    private final List<MVTStyleLayer> styleLayers;
    private final MVTStyleResources resources;

    public MVTLayerData(List<MVTTile> tiles, List<MVTStyleLayer> styleLayers,
            MVTStyleResources resources) {
        this.tiles = tiles;
        this.styleLayers = styleLayers;
        this.resources = resources;
    }

    public static MVTLayerData empty() {
        return new MVTLayerData(Collections.emptyList(), Collections.emptyList(),
                MVTStyleResources.empty());
    }

    public List<MVTTile> getTiles() {
        return tiles;
    }

    public List<MVTStyleLayer> getStyleLayers() {
        return styleLayers;
    }

    public MVTStyleResources getResources() {
        return resources;
    }

    public boolean isEmpty() {
        return tiles.isEmpty() || styleLayers.isEmpty();
    }
}

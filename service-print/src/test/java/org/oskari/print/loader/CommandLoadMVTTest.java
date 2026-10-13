package org.oskari.print.loader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.service.mvt.TileCoord;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;

public class CommandLoadMVTTest {

    @Test
    public void layerParamsAreAddedToTheQueryString() {
        OskariLayer layer = new OskariLayer();
        layer.setUrl("https://example.org/tiles/{z}/{x}/{y}.pbf");
        layer.setParams(JSONHelper.createJSONObject("{\"key\": \"abc\"}"));
        String url = CommandLoadMVT.getTileURL(CommandLoadMVT.getLayerURL(layer),
                new TileCoord(5, 12, 7));
        Assertions.assertEquals("https://example.org/tiles/5/12/7.pbf?key=abc", url,
                "the api key in params reaches the tile request");
    }
}

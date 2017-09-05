package org.oskari.print.wmts;

import org.junit.Assert;
import org.junit.Test;

public class TileMatrixSetCacheTest {

    @Test
    public void getWMTSGetCapabiltiesDefaultCase() {
        String url = "http://www.maps.bob/wmts/1.0.0/{layer}/{style}/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png";
        String expected = "http://www.maps.bob/wmts/1.0.0/WMTSCapabilities.xml";
        String actual = TileMatrixSetCache.getWMTSGetCapabilitiesUri(url);
        Assert.assertEquals(expected, actual);
    }

}

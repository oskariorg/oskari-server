package org.oskari.print.wmts;

import fi.nls.oskari.wmts.domain.TileMatrix;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class WMTSTileMatrixSetParserTest {
    
    @Test
    public void works() throws ParserConfigurationException, SAXException, IOException {
        TileMatrixSet expected = new TileMatrixSet();
        expected.setId("ETRS-TM35FIN");
        expected.setCrs("urn:ogc:def:crs:EPSG:6.3:3067");
        
        double scaleDenominator = 29257142.85714285820722579956;
        for (int i = 0; i <= 15; i++) {
            TileMatrix tileMatrix = new TileMatrix();
            tileMatrix.setId(Integer.toString(i));
            tileMatrix.setScaleDenominator(scaleDenominator);
            tileMatrix.setTopLeftCorner(-548576.0, 8388608.0);
            tileMatrix.setTileWidth(256);
            tileMatrix.setTileHeight(256);
            tileMatrix.setMatrixWidth(1 << i);
            tileMatrix.setMatrixHeight(1 << i);
            expected.addTileMatrix(tileMatrix);
            scaleDenominator /= 2;
        }
        
        List<TileMatrixSet> list;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("WMTSCapabilities.xml")) {
            list = WMTSTileMatrixSetParser.parse(in);
        }
        
        assertEquals(1, list.size());
        TileMatrixSet actual = list.get(0);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCrs(), actual.getCrs());
        
        Map<String, TileMatrix> eTileMatrises = expected.getTileMatrixMap();
        Map<String, TileMatrix> aTileMatrises = actual.getTileMatrixMap();
        assertEquals(eTileMatrises.size(), aTileMatrises.size());
        for (String key : eTileMatrises.keySet()) {
            assertTrue(aTileMatrises.containsKey(key));
            TileMatrix eMatrix = eTileMatrises.get(key);
            TileMatrix aMatrix = aTileMatrises.get(key);
            assertEquals(eMatrix.getId(), aMatrix.getId());
            assertEquals(eMatrix.getScaleDenominator(), aMatrix.getScaleDenominator(), 0);
            assertEquals(eMatrix.getMatrixWidth(), aMatrix.getMatrixWidth());
            assertEquals(eMatrix.getMatrixHeight(), aMatrix.getMatrixHeight());
            assertEquals(eMatrix.getTileWidth(), aMatrix.getTileWidth());
            assertEquals(eMatrix.getTileHeight(), aMatrix.getTileHeight());
            assertEquals(eMatrix.getId(), aMatrix.getId());
            assertArrayEquals(eMatrix.getTopLeftCorner(), aMatrix.getTopLeftCorner(), 0);
        }
    }

}

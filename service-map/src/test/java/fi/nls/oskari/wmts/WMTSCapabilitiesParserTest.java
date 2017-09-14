package fi.nls.oskari.wmts;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.nls.oskari.wmts.domain.TileMatrix;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;

public class WMTSCapabilitiesParserTest {

    @Test
    public void parsesTileMatrixSetInformationCorrectly() throws IOException, IllegalArgumentException, XMLStreamException {
        List<TileMatrix> tileMatrises = new ArrayList<>(16);

        final double[] topLeftCorner = { -548576.0, 8388608.0 };
        double scaleDenominator = 29257142.85714285820722579956;
        for (int i = 0; i <= 15; i++) {
            int len = 1 << i;
            tileMatrises.add(new TileMatrix("" + i, scaleDenominator, topLeftCorner, 256, 256, len, len));
            scaleDenominator /= 2;
        }
        TileMatrixSet expected = new TileMatrixSet("ETRS-TM35FIN",
                "urn:ogc:def:crs:EPSG:6.3:3067", tileMatrises);

        WMTSCapabilities wmtsCapabilities;
        try (InputStream in = new BufferedInputStream(
                getClass().getClassLoader().getResourceAsStream("WMTSCapabilities.xml"))) {
            wmtsCapabilities = WMTSCapabilitiesParser.parseCapabilities(in);
        }
        Collection<TileMatrixSet> tileMatrixSets = wmtsCapabilities.getTileMatrixSets();

        assertEquals(1, tileMatrixSets.size());
        TileMatrixSet actual = tileMatrixSets.iterator().next();
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

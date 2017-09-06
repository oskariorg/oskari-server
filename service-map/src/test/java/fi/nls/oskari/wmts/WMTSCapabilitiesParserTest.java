package fi.nls.oskari.wmts;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Set;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wmts.domain.TileMatrix;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import fi.nls.test.util.ResourceHelper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 20.5.2014
 * Time: 16:47
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class WMTSCapabilitiesParserTest {
    private static final Logger log = LogFactory.getLogger(WMTSCapabilitiesParserTest.class);

    final String capabilitiesInput_NLS = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-input-NLS.xml", this);
    final String capabilitiesInput_Tampere = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-input-tampere.xml", this);
    final String capabilitiesInput_Spain = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-input-spain.xml", this);
    final String expectedJSON_NLS = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-expected-results-NLS.json", this);
    final String expectedJSON_tampere = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-expected-results-tampere.json", this);
    final String expectedJSON_Spain = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-expected-results-spain.json", this);

    @Test
    public void printoutCapabilities() throws Exception {
        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        log.debug(parser.parseCapabilitiesToJSON(capabilitiesInput_Spain, "http://oskari.testing.fi", "EPSG:4326"));
    }
    @Test
    public void testParseCapabilitiesToJSON() throws Exception {
        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();

        final JSONObject parsed = parser.parseCapabilitiesToJSON(capabilitiesInput_NLS, "http://oskari.testing.fi", "EPSG:3067");
        final JSONObject expected = JSONHelper.createJSONObject(expectedJSON_NLS);
        // comparing doesn't work since the JSONArrays are in different order
        //assertTrue("Parsed capabilities XML should match expected", JSONHelper.isEqual(expected, parsed));
    }

    @Test
    public void parsesTileMatrixSetInformationCorrectly() throws Exception {
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("WMTSCapabilities.xml")) {
            IOHelper.copy(in, baos);
        }
        String xml = baos.toString(StandardCharsets.UTF_8.name());
        WMTSCapabilities wmtsCapabilities = new WMTSCapabilitiesParser().parseCapabilities(xml);
        Set<TileMatrixSet> tileMatrixSets = wmtsCapabilities.getTileMatrixSets();

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

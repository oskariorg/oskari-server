package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.oskari.capabilities.LayerCapabilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WMTSCapabilitiesParserTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void parseLayers() throws IOException, ServiceException {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-dummy-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-dummy-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find a layer", 1, layers.size());
        String json = MAPPER.writeValueAsString(layers.values().iterator().next());
        System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(JSONHelper.createJSONObject(json), JSONHelper.createJSONObject(expected)));
    }


    @Test
    public void testASDIParsing() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-asdi-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-asdi-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find a layer", 1, layers.size());

        String json = MAPPER.writeValueAsString(layers.values().iterator().next());
        //System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(JSONHelper.createJSONObject(json), JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void testParsingMapServerMultiColonTileMatrix_LMI() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-LMI-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-LMI-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find 23 layers", 23, layers.size());

        String json = MAPPER.writeValueAsString(layers.get("LMI_Island_einfalt"));
        //System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(JSONHelper.createJSONObject(json), JSONHelper.createJSONObject(expected)));
        // Just testing we are not throwing exception for having tilematrix reference of "EPSG:3057:0"
        /*
        Limit of "EPSG:3857:0" on layer:

            <TileMatrixLimits>
                <TileMatrix>EPSG:3857:0</TileMatrix>

        Should map to:

        <TileMatrixSet>
            <ows:Identifier>EPSG:3857</ows:Identifier>
            ...
            <TileMatrix>
                <ows:Identifier>0</ows:Identifier>
         */
    }

    @Test
    public void testAsJSON_NLS() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-NLS-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-NLS-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find 3 layers", 3, layers.size());

        LayerCapabilities caps = layers.get("taustakartta");

        String json = MAPPER.writeValueAsString(caps);
        //System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(JSONHelper.createJSONObject(json), JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void testAsJSON_Tampere() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-tampere-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-tampere-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find 18 layers", 18, layers.size());

        LayerCapabilities caps = layers.get("tampere:tampere_vkartta_gk24");

        String json = MAPPER.writeValueAsString(caps);
        //System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(JSONHelper.createJSONObject(json), JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void testAsJSON_Spain() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-spain-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-spain-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find 2 layers", 2, layers.size());

        LayerCapabilities caps = layers.get("IGNBaseTodo");

        String json = MAPPER.writeValueAsString(caps);
        //System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(JSONHelper.createJSONObject(json), JSONHelper.createJSONObject(expected)));
    }

    private void compareMatrixSets(JSONObject expected, JSONObject actual) throws JSONException {
        JSONObject msActual = actual.getJSONObject("matrixSets");
        JSONObject msExpected = expected.getJSONObject("matrixSets");
        Iterator<String> matrixSetIds = msExpected.keys();
        while (matrixSetIds.hasNext()) {
            String id = matrixSetIds.next();
            JSONObject tms1 = msExpected.getJSONObject(id);
            JSONObject tms2 = msActual.getJSONObject(id);
            String identifier1 = tms1.getString("identifier");
            String identifier2 = tms2.getString("identifier");
            assertEquals(identifier1, identifier2);
            String proj1 = tms1.getString("projection");
            String proj2 = tms2.getString("projection");
            assertEquals(proj1, proj2);
            JSONArray matrixIds1 = tms1.getJSONArray("matrixIds");
            JSONArray matrixIds2 = tms2.getJSONArray("matrixIds");
            assertEquals(matrixIds1.length(), matrixIds2.length());
            for (int i = 0; i < matrixIds1.length(); i++) {
                JSONObject matrix1 = matrixIds1.getJSONObject(i);
                JSONObject matrix2 = findByIdentifier(matrixIds2, matrix1.getString("identifier"));
                assertTrue(JSONHelper.isEqual(matrix1, matrix2));
            }
        }
    }

    private JSONObject findByIdentifier(JSONArray matrixIds, String identifier) throws JSONException {
        for (int i = 0; i < matrixIds.length(); i++) {
            JSONObject matrix = matrixIds.getJSONObject(i);
            if (identifier.equals(matrix.getString("identifier"))) {
                return matrix;
            }
        }
        return null;
    }

/*
    @Test
    public void parsesTileMatrixSetInformationCorrectly() throws IOException, IllegalArgumentException, XMLStreamException {
        List<TileMatrix> tileMatrices = new ArrayList<>(16);

        final double[] topLeftCorner = { -548576.0, 8388608.0 };
        double scaleDenominator = 29257142.85714285820722579956;
        for (int i = 0; i <= 15; i++) {
            int len = 1 << i;
            tileMatrices.add(new TileMatrix("" + i, scaleDenominator, topLeftCorner, 256, 256, len, len));
            scaleDenominator /= 2;
        }
        TileMatrixSet expected = new TileMatrixSet("ETRS-TM35FIN",
                "urn:ogc:def:crs:EPSG:6.3:3067", tileMatrices);

        WMTSCapabilities wmtsCapabilities;
        try (InputStream in = new BufferedInputStream(
                getClass().getClassLoader().getResourceAsStream("WMTSCapabilitiesParserTest-dummy-input.xml"))) {
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

*/
}
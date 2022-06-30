package org.oskari.capabilities.ogc;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WMTSCapabilitiesParserTest {

    private static final Set<String> SYSTEM_CRS = new HashSet<>(5);

    @BeforeClass
    public static void init() {
        SYSTEM_CRS.add("EPSG:3857");
        SYSTEM_CRS.add("EPSG:3067");
    }

    @Test
    public void parseLayers() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-dummy-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-dummy-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find a layer", 1, layers.size());
        JSONObject json = CapabilitiesService.toJSON(layers.values().iterator().next(), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void testNASAParsing() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-nasa-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-nasa-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find a bunch of layers", 965, layers.size());
        JSONObject json = CapabilitiesService.toJSON(layers.get("BlueMarble_NextGeneration"), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void testASDIParsing() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-asdi-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-asdi-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find a layer", 1, layers.size());

        JSONObject json = CapabilitiesService.toJSON(layers.values().iterator().next(), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }
    @Test
    public void testVaylaParsing() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-Vayla-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-Vayla-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find a layer", 44, layers.size());

        JSONObject json = CapabilitiesService.toJSON(layers.values().iterator().next(), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void testParsingMapServerMultiColonTileMatrix_LMI() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-LMI-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-LMI-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find 23 layers", 23, layers.size());

        JSONObject json = CapabilitiesService.toJSON(layers.get("LMI_Island_einfalt"), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
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

        JSONObject json = CapabilitiesService.toJSON(layers.get("taustakartta"), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));

        LayerCapabilitiesWMTS caps = CapabilitiesService.fromJSON(json.toString(), OskariLayer.TYPE_WMTS);
        // Deserialization back to objects succeeded \o/
        assertEquals("Only one tilematrix after filtering against CRS list for system", 1, caps.getTileMatrixLinks().size());
    }

    @Test
    public void testAsJSON_Tampere() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-tampere-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-tampere-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find 18 layers", 18, layers.size());

        JSONObject json = CapabilitiesService.toJSON(layers.get("tampere:tampere_vkartta_gk24"), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void testAsJSON_Spain() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-spain-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-spain-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find 2 layers", 2, layers.size());

        JSONObject json = CapabilitiesService.toJSON(layers.get("IGNBaseTodo"), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }


}
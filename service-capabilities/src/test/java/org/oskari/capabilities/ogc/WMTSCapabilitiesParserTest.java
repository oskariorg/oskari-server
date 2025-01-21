package org.oskari.capabilities.ogc;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WMTSCapabilitiesParserTest {

    private static final Set<String> SYSTEM_CRS = new HashSet<>(5);

    @BeforeAll
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
        Assertions.assertEquals(1, layers.size(), "Should find a layer");
        JSONObject json = CapabilitiesService.toJSON(layers.values().iterator().next(), SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }

    @Test
    public void testNASAParsing() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-nasa-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-nasa-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(965, layers.size(), "Should find a bunch of layers");
        JSONObject json = CapabilitiesService.toJSON(layers.get("BlueMarble_NextGeneration"), SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }

    @Test
    public void testASDIParsing() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-asdi-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-asdi-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(1, layers.size(), "Should find a layer");

        JSONObject json = CapabilitiesService.toJSON(layers.values().iterator().next(), SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }
    @Test
    public void testVaylaParsing() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-Vayla-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-Vayla-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(44, layers.size(), "Should find a layer");

        JSONObject json = CapabilitiesService.toJSON(layers.values().iterator().next(), SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }

    @Test
    public void testParsingMapServerMultiColonTileMatrix_LMI() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-LMI-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-LMI-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(23, layers.size(), "Should find 23 layers");

        JSONObject json = CapabilitiesService.toJSON(layers.get("LMI_Island_einfalt"), SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
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
        Assertions.assertEquals(3, layers.size(), "Should find 3 layers");

        JSONObject json = CapabilitiesService.toJSON(layers.get("taustakartta"), SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");

        LayerCapabilitiesWMTS caps = CapabilitiesService.fromJSON(json.toString(), OskariLayer.TYPE_WMTS);
        // Deserialization back to objects succeeded \o/
        Assertions.assertEquals(1, caps.getTileMatrixLinks().size(), "Only one tilematrix after filtering against CRS list for system");
    }

    @Test
    public void testAsJSON_Tampere() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-tampere-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-tampere-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(18, layers.size(), "Should find 18 layers");

        JSONObject json = CapabilitiesService.toJSON(layers.get("tampere:tampere_vkartta_gk24"), SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }

    @Test
    public void testAsJSON_Spain() throws Exception {
        String xml = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-spain-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMTSCapabilitiesParserTest-spain-expected.json", this);

        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        Assertions.assertEquals(2, layers.size(), "Should find 2 layers");

        JSONObject json = CapabilitiesService.toJSON(layers.get("IGNBaseTodo"), SYSTEM_CRS);
        // System.out.println(json);
        Assertions.assertTrue(JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)), "JSON should match");
    }


}
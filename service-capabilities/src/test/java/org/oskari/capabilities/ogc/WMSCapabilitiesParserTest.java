package org.oskari.capabilities.ogc;

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

public class WMSCapabilitiesParserTest {

    private static final Set<String> SYSTEM_CRS = new HashSet<>(5);
    private WMSCapabilitiesParser parser = new WMSCapabilitiesParser();

    @BeforeClass
    public static void init() {
        SYSTEM_CRS.add("EPSG:3857");
        SYSTEM_CRS.add("EPSG:3067");
    }

    @Test
    public void parseCp1_1_1() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-cp_1_1_1-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-cp-expected.json", this);

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find layers", 2, layers.size());
        JSONObject json = CapabilitiesService.toJSON(layers.values().iterator().next(), SYSTEM_CRS);
        JSONObject expectedJSON = JSONHelper.createJSONObject(expected);
        // Note! 1.1.1 doesn't have the metadata url
        expectedJSON.optJSONObject("typeSpecific").remove("metadataUrl");
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, expectedJSON));
    }

    @Test
    public void parseCp1_3_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-cp_1_3_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-cp-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find layers", 2, layers.size());
        JSONObject json = CapabilitiesService.toJSON(layers.values().iterator().next(), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void parseChloro1_1_1() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-chloro_1_1_1-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-chloro-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find layers", 9, layers.size());
        JSONObject json = CapabilitiesService.toJSON(layers.get("arctic_sdi:SeaSurfaceTemperature"), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void parseChloro1_3_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-chloro_1_3_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-chloro-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find layers", 9, layers.size());
        JSONObject json = CapabilitiesService.toJSON(layers.get("arctic_sdi:SeaSurfaceTemperature"), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }

    @Test
    public void parseDuplicatedName() throws Exception {
        String xml = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-capabilities-with-duplicated-layername-input.xml", this);
        String expected = ResourceHelper.readStringResource("WMSCapabilitiesParserTest-capabilities-with-duplicated-layername-expected.json", this);

        WMSCapabilitiesParser parser = new WMSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find a layer", 25, layers.size());

        JSONObject json = CapabilitiesService.toJSON(layers.get("muinaismuistot"), SYSTEM_CRS);
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, JSONHelper.createJSONObject(expected)));
    }

}
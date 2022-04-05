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

import static org.junit.Assert.*;

public class WFSCapabilitiesParserTest {

    private static final Set<String> SYSTEM_CRS = new HashSet<>(5);
    private WFSCapabilitiesParser parser = new WFSCapabilitiesParser();

    @BeforeClass
    public static void init() {
        SYSTEM_CRS.add("EPSG:3857");
        SYSTEM_CRS.add("EPSG:3067");
    }

    @Test
    public void parseStatFi1_1_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-1_1_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-expected.json", this);

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find 297 layers", 297, layers.size());
        LayerCapabilitiesWFS layerCaps = (LayerCapabilitiesWFS) layers.get("tilastointialueet:avi4500k");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        JSONObject expectedJSON = JSONHelper.createJSONObject(expected);
        // Check and remove version as it is different on expected between 1.1.0 and 2.0.0 input
        assertEquals("Check version", "1.1.0", json.optJSONObject("typeSpecific").remove("version"));
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, expectedJSON));

        String wkt = "POLYGON ((15.999210419254936 56.23928539106909, 15.999210419254936 73.5170461466599, 33.27697117484574 73.5170461466599, 33.27697117484574 56.23928539106909, 15.999210419254936 56.23928539106909))";
        assertEquals("Coverage should match", wkt, layerCaps.getBbox().getWKT());
    }

    @Test
    public void parseStatFi2_0_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-2_0_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-expected.json", this);

        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find 297 layers", 297, layers.size());
        LayerCapabilitiesWFS layerCaps = (LayerCapabilitiesWFS) layers.get("tilastointialueet:avi4500k");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        JSONObject expectedJSON = JSONHelper.createJSONObject(expected);
        // Check and remove version as it is different on expected between 1.1.0 and 2.0.0 input
        assertEquals("Check version", "2.0.0", json.optJSONObject("typeSpecific").remove("version"));
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, expectedJSON));

        String wkt = "POLYGON ((15.999210419254936 56.23928539106909, 15.999210419254936 73.5170461466599, 33.27697117484574 73.5170461466599, 33.27697117484574 56.23928539106909, 15.999210419254936 56.23928539106909))";
        assertEquals("Coverage should match", wkt, layerCaps.getBbox().getWKT());
    }
}
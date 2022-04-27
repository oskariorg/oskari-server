package org.oskari.capabilities.ogc;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.wfs.DescribeFeatureTypeProvider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WFSCapabilitiesParserTest {

    private static final Set<String> SYSTEM_CRS = new HashSet<>(5);

    private ServiceConnectInfo getConnectInfo(String version) {
        return new ServiceConnectInfo("https://mydomain.org", OskariLayer.TYPE_WFS, version);
    }
    @BeforeClass
    public static void init() {
        SYSTEM_CRS.add("EPSG:3857");
        SYSTEM_CRS.add("EPSG:3067");
        SYSTEM_CRS.add("EPSG:4326");
    }

    private WFSCapabilitiesParser getParser() {
        return getParser(null);
    }
    private WFSCapabilitiesParser getParser(String describeResponse) {
        // spy to just override the single method
        WFSCapabilitiesParser parser = new WFSCapabilitiesParser();
        parser.setDescribeFeatureTypeProvider(new DescribeFeatureTypeProviderMock(describeResponse));
        return parser;
    }

    @Test
    public void parseStatFi1_1_0() throws Exception {
        String version = "1.1.0";
        String xml = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-1_1_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-expected.json", this);

        Map<String, LayerCapabilities> layers = getParser().parseLayers(xml, version, getConnectInfo(version));
        assertEquals("Should find 297 layers", 297, layers.size());
        LayerCapabilitiesWFS layerCaps = (LayerCapabilitiesWFS) layers.get("tilastointialueet:avi4500k");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        JSONObject expectedJSON = JSONHelper.createJSONObject(expected);
        // Check and remove version as it is different on expected between 1.1.0 and 2.0.0 input
        assertEquals("Check version", version, json.optJSONObject("typeSpecific").remove("version"));
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, expectedJSON));

        String wkt = "POLYGON ((15.999210419254936 56.23928539106909, 15.999210419254936 73.5170461466599, 33.27697117484574 73.5170461466599, 33.27697117484574 56.23928539106909, 15.999210419254936 56.23928539106909))";
        assertEquals("Coverage should match", wkt, layerCaps.getBbox().getWKT());

        LayerCapabilitiesWFS deserialized = CapabilitiesService.fromJSON(json.toString(), "wfslayer");
        assertTrue("Typed correctly on deserialization", deserialized instanceof LayerCapabilitiesWFS);
    }

    @Test
    public void parseStatFi2_0_0() throws Exception {
        String version = "2.0.0";
        String xml = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-2_0_0-input.xml", this);
        String expected = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-expected.json", this);

        Map<String, LayerCapabilities> layers = getParser().parseLayers(xml, version, getConnectInfo(version));
        assertEquals("Should find 297 layers", 297, layers.size());
        LayerCapabilitiesWFS layerCaps = (LayerCapabilitiesWFS) layers.get("tilastointialueet:avi4500k");
        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        JSONObject expectedJSON = JSONHelper.createJSONObject(expected);
        // Check and remove version as it is different on expected between 1.1.0 and 2.0.0 input
        assertEquals("Check version", version, json.optJSONObject("typeSpecific").remove("version"));
        // System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(json, expectedJSON));

        String wkt = "POLYGON ((15.999210419254936 56.23928539106909, 15.999210419254936 73.5170461466599, 33.27697117484574 73.5170461466599, 33.27697117484574 56.23928539106909, 15.999210419254936 56.23928539106909))";
        assertEquals("Coverage should match", wkt, layerCaps.getBbox().getWKT());
    }

    @Test
    public void parseStatFi3_0_0() throws Exception {
        String version = "3.0.0";
        String serviceJSON = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-3_0_0-input.json", this);
        String expected = ResourceHelper.readStringResource("WFSCapabilitiesParserTest-statfi-3_0_0-expected.json", this);

        WFSCapabilitiesParser parser = getParser();
        Map<String, LayerCapabilities> layers = parser.parseLayers(serviceJSON, version, getConnectInfo(version));
        assertEquals("Should find 19 layers", 19, layers.size());
        LayerCapabilitiesWFS layerCaps = (LayerCapabilitiesWFS) layers.get("AreaStatisticalUnit_4500k_EPSG_4326_2020");

        JSONObject json = CapabilitiesService.toJSON(layerCaps, SYSTEM_CRS);
        // System.out.println(json);
        JSONObject expectedJSON = JSONHelper.createJSONObject(expected);
        assertTrue("JSON should match", JSONHelper.isEqual(json, expectedJSON));
    }

    class DescribeFeatureTypeProviderMock extends DescribeFeatureTypeProvider {
        private String content;

        public DescribeFeatureTypeProviderMock(String content) {
            this.content = content;
        }
        @Override
        public String getDescribeContent(String url, String user, String pass) throws IOException {
            return content;
        }
    }
}
package org.oskari.capabilities.ogc.wfs;

import fi.nls.test.util.ResourceHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescribeFeatureTypeParserTest {

    private Map<String, String> getExpected() {
        Map<String, String> expected = new HashMap<>();
        expected.put("avi", "string");
        expected.put("vuosi", "int");
        expected.put("nimi", "string");
        expected.put("namn", "string");
        expected.put("name", "string");
        expected.put("geom", "GeometryPropertyType");
        return expected;
    }

    @Test
    public void parseFeatureType() throws Exception {
        String xml = ResourceHelper.readStringResource("WFSDescribeFeatureTypeParserTest-statfi-1_1_0-input.xml", this);
        List<FeaturePropertyType> props = DescribeFeatureTypeParser.parseFeatureType(xml, "tilastointialueet:avi4500k");
        verify(props);
    }
    @Test
    public void parseFeatureType2_0_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WFSDescribeFeatureTypeParserTest-statfi-2_0_0-input.xml", this);
        List<FeaturePropertyType> props = DescribeFeatureTypeParser.parseFeatureType(xml, "tilastointialueet:avi4500k");
        verify(props);
    }

    private void verify(List<FeaturePropertyType> props, Map<String, String> expected) {
        Assertions.assertEquals(expected.size(), props.size(), "Should get expected amount of props");

        props.stream().forEach(p -> {
            Assertions.assertEquals(expected.get(p.name), p.type, "Prop " + p.name + " should be " + p.type);
        });
    }
    private void verify(List<FeaturePropertyType> props) {
        verify(props, getExpected());
    }

    @Test
    public void parseArcgisFeatureType2_0_0() throws Exception {
        String xml = ResourceHelper.readStringResource("WFSDescribeFeatureTypeParserTest-restrictions-2_0_0-input.xml", this);
        List<FeaturePropertyType> props = DescribeFeatureTypeParser.parseFeatureType(xml, "tutkitut_turvealueet");

        Map<String, String> expected = new HashMap<>();
        expected.put("OBJECTID", "int");
        expected.put("SUON_ID", "int");
        expected.put("SUON_NIMI", "string");
        expected.put("TUTKIMUSVUOSI", "int");
        expected.put("Shape", "PointPropertyType");
        verify(props, expected);
    }

}
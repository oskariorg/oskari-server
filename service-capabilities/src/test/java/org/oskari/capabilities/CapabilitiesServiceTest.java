package org.oskari.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.test.util.ResourceHelper;
import junit.framework.TestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMTS;
import org.oskari.capabilities.ogc.wfs.FeaturePropertyType;
import org.oskari.capabilities.ogc.wmts.ResourceUrl;

import java.util.HashMap;
import java.util.Map;

public class CapabilitiesServiceTest extends TestCase {

    @Test
    public void testShortSyntax()  {
        Map<String, String> expected = new HashMap<>();
        expected.put("EPSG:3067", "urn:ogc:def:crs:EPSG:6.3:3067"); // nlsfi
        expected.put("EPSG:3857", "urn:ogc:def:crs:EPSG:6.18:3:3857"); // nasa
        expected.put("EPSG:3575", "urn:ogc:def:crs:EPSG::3575"); // asdi
        expected.put("EPSG:3067", "urn:x-ogc:def:crs:EPSG:3067"); // statfi wfs 1.1

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            Assertions.assertEquals(entry.getKey(), CapabilitiesService.shortSyntaxEpsg(entry.getValue()));
        }
        Assertions.assertNull(CapabilitiesService.shortSyntaxEpsg(null), "Null should return null");
        //assertNull("Random stuff should return null", ProjectionHelper.shortSyntaxEpsg("SG_ASegASEgae_:aeg:age:h4:4"));
        Assertions.assertEquals("EPSG:3067", CapabilitiesService.shortSyntaxEpsg("asg:sgr:rej:J:EPSG:3067"), "This might be weird but being lenient");
        Assertions.assertEquals("EPSG:3067", CapabilitiesService.shortSyntaxEpsg("asg:sgr:rej:J:EPSG:235:235.6:3067"), "Can be 3 parts after EPSG");
        Assertions.assertEquals("urn:ogc:def:crs:EPSG3067", CapabilitiesService.shortSyntaxEpsg("urn:ogc:def:crs:EPSG3067"), "Unparseable returns as is");

        Assertions.assertEquals("EPSG:3067", CapabilitiesService.shortSyntaxEpsg("http://www.opengis.net/def/crs/EPSG/0/3067"), "Should parse from url");
    }
    @Test
    public void testDeserializationWFS_2_0_0()  {
        String json = ResourceHelper.readStringResource("Capabilities_WFS_2_0_0.json", this);
        LayerCapabilitiesWFS caps = CapabilitiesService.fromJSON(json, OskariLayer.TYPE_WFS);
        Assertions.assertEquals(32, caps.getFeatureProperties().size());
        Assertions.assertEquals("the_geom", caps.getGeometryField());
        FeaturePropertyType prop = caps.getFeatureProperty("kuntanumero");
        Assertions.assertNotNull(prop, "Should have kuntanumero");
        Assertions.assertEquals("int", prop.type, "kuntanumero should be of type int");
    }

    @Test
    public void testDeserializationWFS_3_0_0()  {
        String json = ResourceHelper.readStringResource("Capabilities_OGCAPIFeatures.json", this);
        LayerCapabilitiesWFS caps = CapabilitiesService.fromJSON(json, OskariLayer.TYPE_WFS);
        Assertions.assertEquals(40, caps.getFeatureProperties().size());
        Assertions.assertEquals("geometry", caps.getGeometryField());
        FeaturePropertyType prop = caps.getFeatureProperty("state");
        Assertions.assertNotNull(prop, "Should have state");
        Assertions.assertEquals("number", prop.type, "state should be of type number");
    }
    @Test
    public void testDeserializationWMTS()  {
        String json = ResourceHelper.readStringResource("Capabilities_WMTS.json", this);
        LayerCapabilitiesWMTS caps = CapabilitiesService.fromJSON(json, OskariLayer.TYPE_WMTS);
        Assertions.assertEquals(15, caps.getTileMatrixLinks().size());
        Assertions.assertEquals("EPSG:3067", caps.getTileMatrixLinks().stream()
            .map(l -> l.getTileMatrixSet().getShortCrs())
            .filter(srs -> srs.equals("EPSG:3067"))
            .findFirst()
            .orElse(null));
        ResourceUrl url = caps.getResourceUrl("tile");
        Assertions.assertNotNull(url, "Should have tile url");
        Assertions.assertEquals("https://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/taustakartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png", url.getTemplate());
    }
    @Test
    public void testDeserializationWMTSWithLimits()  {
        String json = ResourceHelper.readStringResource("ogc/WMTSCapabilitiesParserTest-Vayla-expected.json", this);
        LayerCapabilitiesWMTS caps = CapabilitiesService.fromJSON(json, OskariLayer.TYPE_WMTS);
        Assertions.assertEquals(3, caps.getTileMatrixLinks().size());
        Assertions.assertEquals("EPSG:3067", caps.getTileMatrixLinks().stream()
                .map(l -> l.getTileMatrixSet().getShortCrs())
                .filter(srs -> srs.equals("EPSG:3067"))
                .findFirst()
                .orElse(null));
        ResourceUrl url = caps.getResourceUrl("tile");
        Assertions.assertNotNull(url, "Should have tile url");
        Assertions.assertEquals("https://julkinen.traficom.fi/rasteripalvelu/wmts/rest/Traficom:Merikarttasarja B erikoiskartat/{style}/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}?format=image/png", url.getTemplate());
    }
}
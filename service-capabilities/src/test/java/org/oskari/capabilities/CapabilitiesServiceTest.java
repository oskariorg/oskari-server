package org.oskari.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.test.util.ResourceHelper;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMTS;
import org.oskari.capabilities.ogc.wfs.FeaturePropertyType;
import org.oskari.capabilities.ogc.wmts.ResourceUrl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class CapabilitiesServiceTest extends TestCase {

    @Test
    public void testShortSyntax()  {
        Map<String, String> expected = new HashMap<>();
        expected.put("EPSG:3067", "urn:ogc:def:crs:EPSG:6.3:3067"); // nlsfi
        expected.put("EPSG:3857", "urn:ogc:def:crs:EPSG:6.18:3:3857"); // nasa
        expected.put("EPSG:3575", "urn:ogc:def:crs:EPSG::3575"); // asdi
        expected.put("EPSG:3067", "urn:x-ogc:def:crs:EPSG:3067"); // statfi wfs 1.1

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            assertEquals(entry.getKey(), CapabilitiesService.shortSyntaxEpsg(entry.getValue()));
        }
        assertNull("Null should return null", CapabilitiesService.shortSyntaxEpsg(null));
        //assertNull("Random stuff should return null", ProjectionHelper.shortSyntaxEpsg("SG_ASegASEgae_:aeg:age:h4:4"));
        assertEquals("This might be weird but being lenient", "EPSG:3067", CapabilitiesService.shortSyntaxEpsg("asg:sgr:rej:J:EPSG:3067"));
        assertEquals("Can be 3 parts after EPSG", "EPSG:3067", CapabilitiesService.shortSyntaxEpsg("asg:sgr:rej:J:EPSG:235:235.6:3067"));
        assertEquals("Unparseable returns as is", "urn:ogc:def:crs:EPSG3067", CapabilitiesService.shortSyntaxEpsg("urn:ogc:def:crs:EPSG3067"));

        assertEquals("Should parse from url", "EPSG:3067", CapabilitiesService.shortSyntaxEpsg("http://www.opengis.net/def/crs/EPSG/0/3067"));
    }
    @Test
    public void testDeserializationWFS_2_0_0()  {
        String json = ResourceHelper.readStringResource("Capabilities_WFS_2_0_0.json", this);
        LayerCapabilitiesWFS caps = CapabilitiesService.fromJSON(json, OskariLayer.TYPE_WFS);
        Assert.assertEquals(32, caps.getFeatureProperties().size());
        Assert.assertEquals("the_geom", caps.getGeometryField());
        FeaturePropertyType prop = caps.getFeatureProperty("kuntanumero");
        assertNotNull("Should have kuntanumero", prop);
        assertEquals("kuntanumero should be of type int","int",prop.type);
    }

    @Test
    public void testDeserializationWFS_3_0_0()  {
        String json = ResourceHelper.readStringResource("Capabilities_OGCAPIFeatures.json", this);
        LayerCapabilitiesWFS caps = CapabilitiesService.fromJSON(json, OskariLayer.TYPE_WFS);
        Assert.assertEquals(40, caps.getFeatureProperties().size());
        Assert.assertEquals("geometry", caps.getGeometryField());
        FeaturePropertyType prop = caps.getFeatureProperty("state");
        assertNotNull("Should have state", prop);
        assertEquals("state should be of type number","number",prop.type);
    }
    @Test
    public void testDeserializationWMTS()  {
        String json = ResourceHelper.readStringResource("Capabilities_WMTS.json", this);
        LayerCapabilitiesWMTS caps = CapabilitiesService.fromJSON(json, OskariLayer.TYPE_WMTS);
        Assert.assertEquals(15, caps.getTileMatrixLinks().size());
        Assert.assertEquals("EPSG:3067",
                caps.getTileMatrixLinks().stream()
                    .map(l -> l.getTileMatrixSet().getShortCrs())
                    .filter(srs -> srs.equals("EPSG:3067"))
                    .findFirst()
                    .orElse(null));
        ResourceUrl url = caps.getResourceUrl("tile");
        assertNotNull("Should have tile url", url);
        Assert.assertEquals("https://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/taustakartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png", url.getTemplate());
    }
    @Test
    public void testDeserializationWMTSWithLimits()  {
        String json = ResourceHelper.readStringResource("ogc/WMTSCapabilitiesParserTest-Vayla-expected.json", this);
        LayerCapabilitiesWMTS caps = CapabilitiesService.fromJSON(json, OskariLayer.TYPE_WMTS);
        Assert.assertEquals(3, caps.getTileMatrixLinks().size());
        Assert.assertEquals("EPSG:3067",
                caps.getTileMatrixLinks().stream()
                        .map(l -> l.getTileMatrixSet().getShortCrs())
                        .filter(srs -> srs.equals("EPSG:3067"))
                        .findFirst()
                        .orElse(null));
        ResourceUrl url = caps.getResourceUrl("tile");
        assertNotNull("Should have tile url", url);
        Assert.assertEquals("https://julkinen.traficom.fi/rasteripalvelu/wmts/rest/Traficom:Merikarttasarja B erikoiskartat/{style}/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}?format=image/png", url.getTemplate());
    }
}
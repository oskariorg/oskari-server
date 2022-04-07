package org.oskari.capabilities;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class CapabilitiesServiceTest extends TestCase {

    @Test
    public void testGetIdFromMetadataUrl() {
        assertNull("Null metadata returns null id", CapabilitiesService.getIdFromMetadataUrl(null));
        assertNull("Empty metadata returns null id", CapabilitiesService.getIdFromMetadataUrl(""));
        assertEquals("Non http-starting metadata returns as is", "testing", CapabilitiesService.getIdFromMetadataUrl("testing"));
        assertNull("Url without querystring returns null id", CapabilitiesService.getIdFromMetadataUrl("http://mydomain.org"));
        assertNull("Url without id|uuid returns null id", CapabilitiesService.getIdFromMetadataUrl("http://mydomain.org?my=key"));
        assertEquals("Url with id returns id value simple", "key", CapabilitiesService.getIdFromMetadataUrl("http://mydomain.org?uuid=key"));
        Map<String, String> expected = new HashMap<>();
        expected.put("http://mydomain.org?test=test&id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuid=key2&post=test", "key2");
        expected.put("http://mydomain.org?test=test&Id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuId=Key&post=test", "Key");
        for (String url : expected.keySet()) {
            assertEquals("Url with id returns id value", expected.get(url), CapabilitiesService.getIdFromMetadataUrl(url));
        }
    }


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
}
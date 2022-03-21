package org.oskari.capabilities;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


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
}
package org.oskari.capabilities;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.nls.oskari.util.PropertyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MetadataHelperTest {

    @BeforeClass
    public static void setup() throws Exception {
        PropertyUtil.addProperty("service.metadata.domains", "paikkatietohakemisto.fi, mydomain.org", true);
        PropertyUtil.addProperty("service.metadata.url", "http://propertyurl.org", true);
    }

    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testGetIdFromMetadataUrl() {
        assertNull("Null metadata returns null id", MetadataHelper.getIdFromMetadataUrl(null));
        assertNull("Empty metadata returns null id", MetadataHelper.getIdFromMetadataUrl(""));
        assertEquals("Non http-starting metadata returns as is", "testing", MetadataHelper.getIdFromMetadataUrl("testing"));
        assertNull("Url without querystring returns null id", MetadataHelper.getIdFromMetadataUrl("http://mydomain.org"));
        assertNull("Url without id|uuid returns null id", MetadataHelper.getIdFromMetadataUrl("http://mydomain.org?my=key"));
        assertEquals("Url with id returns id value simple", "key", MetadataHelper.getIdFromMetadataUrl("http://mydomain.org?uuid=key"));
        Map<String, String> expected = new HashMap<>();
        expected.put("http://mydomain.org?test=test&id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuid=key2&post=test", "key2");
        expected.put("http://mydomain.org?test=test&Id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuId=Key&post=test", "Key");
        for (String url : expected.keySet()) {
            assertEquals("Url with id returns id value", expected.get(url), MetadataHelper.getIdFromMetadataUrl(url));
        }
    }

    @Test
    public void testIsDomainAllowed() {
        ArrayList<String> allowedDomains = new ArrayList<String>();
        allowedDomains.add("paikkatietohakemisto.fi");
        allowedDomains.add("mydomain.org");
        assertTrue("Url that is included in the url-array returns true", MetadataHelper.isDomainAllowed("http://www.paikkatietohakemisto.fi?uuid=key", allowedDomains));
        assertFalse("Url that is not included in the url-array returns false", MetadataHelper.isDomainAllowed("http://www.unallowed.fi?uuid=key", allowedDomains));
    }
}
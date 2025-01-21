package org.oskari.capabilities;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import fi.nls.oskari.util.PropertyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetadataHelperTest {

    @BeforeAll
    public static void setup() throws Exception {
        PropertyUtil.addProperty("service.metadata.domains", "paikkatietohakemisto.fi, mydomain.org", true);
        PropertyUtil.addProperty("service.metadata.url", "http://propertyurl.org", true);
    }

    @AfterAll
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testGetIdFromMetadataUrl() {
        Assertions.assertNull(MetadataHelper.getIdFromMetadataUrl(null), "Null metadata returns null id");
        Assertions.assertNull(MetadataHelper.getIdFromMetadataUrl(""), "Empty metadata returns null id");
        Assertions.assertEquals("testing", MetadataHelper.getIdFromMetadataUrl("testing"), "Non http-starting metadata returns as is");
        Assertions.assertNull(MetadataHelper.getIdFromMetadataUrl("http://mydomain.org"), "Url without querystring returns null id");
        Assertions.assertNull(MetadataHelper.getIdFromMetadataUrl("http://mydomain.org?my=key"), "Url without id|uuid returns null id");
        Assertions.assertEquals("key", MetadataHelper.getIdFromMetadataUrl("http://mydomain.org?uuid=key"), "Url with id returns id value simple");
        Map<String, String> expected = new HashMap<>();
        expected.put("http://mydomain.org?test=test&id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuid=key2&post=test", "key2");
        expected.put("http://mydomain.org?test=test&Id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuId=Key&post=test", "Key");
        for (String url : expected.keySet()) {
            Assertions.assertEquals(expected.get(url), MetadataHelper.getIdFromMetadataUrl(url), "Url with id returns id value");
        }
    }

    @Test
    public void testIsDomainAllowed() {
        ArrayList<String> allowedDomains = new ArrayList<String>();
        allowedDomains.add("paikkatietohakemisto.fi");
        allowedDomains.add("mydomain.org");
        Assertions.assertTrue(MetadataHelper.isDomainAllowed("http://www.paikkatietohakemisto.fi?uuid=key", allowedDomains), "Url that is included in the url-array returns true");
        Assertions.assertFalse(MetadataHelper.isDomainAllowed("http://www.unallowed.fi?uuid=key", allowedDomains), "Url that is not included in the url-array returns false");
    }
    
    @Test
    public void testGetAllowedDomainsList() {
        ArrayList<String> expected = new ArrayList<String>();
        expected.add("paikkatietohakemisto.fi");
        expected.add("mydomain.org");
        expected.add("http://propertyurl.org");
        Assertions.assertEquals(expected, MetadataHelper.getAllowedDomainsList(), "allowedDomainsList contains expected domain");
    }
}
package fi.nls.oskari.map.layer.formatters;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class LayerJSONFormatterTest {

    @Test
    public void getFixedDataUrl() {
        assertNull("Null metadata returns null id", LayerJSONFormatter.getFixedDataUrl(null));
        assertNull("Empty metadata returns null id", LayerJSONFormatter.getFixedDataUrl(""));
        assertEquals("Non http-starting metadata returns as is", "testing", LayerJSONFormatter.getFixedDataUrl("testing"));
        assertNull("Url without querystring returns null id", LayerJSONFormatter.getFixedDataUrl("http://mydomain.org"));
        assertNull("Url without id|uuid returns null id", LayerJSONFormatter.getFixedDataUrl("http://mydomain.org?my=key"));
        assertEquals("Url with id returns id value simple", "key", LayerJSONFormatter.getFixedDataUrl("http://mydomain.org?uuid=key"));
        Map<String, String> expected = new HashMap<>();
        expected.put("http://mydomain.org?test=test&id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuid=key2&post=test", "key2");
        expected.put("http://mydomain.org?test=test&Id=key&post=test", "key");
        expected.put("http://mydomain.org?test=test&uuId=Key&post=test", "Key");
        for (String url : expected.keySet()) {
            assertEquals("Url with id returns id value", expected.get(url), LayerJSONFormatter.getFixedDataUrl(url));
        }
    }
}
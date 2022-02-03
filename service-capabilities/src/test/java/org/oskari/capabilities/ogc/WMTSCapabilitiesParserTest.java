package org.oskari.capabilities.ogc;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.junit.Test;
import org.oskari.capabilities.LayerCapabilities;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class WMTSCapabilitiesParserTest {

    private final ObjectMapper MAPPER = new ObjectMapper();
    @Test
    public void parseLayers() throws IOException, ServiceException  {
        String xml = IOHelper.readString(WMTSCapabilitiesParserTest.class.getResourceAsStream("WMTSCapabilities.xml"));
        String expected = IOHelper.readString(WMTSCapabilitiesParserTest.class.getResourceAsStream("WMTSCapabilities-expected.json"));
        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        assertEquals("Should find a layer", 1, layers.size());
        String json = MAPPER.writeValueAsString(layers.values().iterator().next());
        System.out.println(json);
        assertTrue("JSON should match", JSONHelper.isEqual(JSONHelper.createJSONObject(json), JSONHelper.createJSONObject(expected)));
    }
}
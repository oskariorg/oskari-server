package org.oskari.capabilities.ogc;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import org.junit.Test;
import org.oskari.capabilities.LayerCapabilities;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class WMTSCapabilitiesParserTest {

    @Test
    public void parseLayers() throws IOException, ServiceException  {
        String xml = IOHelper.readString(WMTSCapabilitiesParserTest.class.getResourceAsStream("WMTSCapabilities.xml"));
        WMTSCapabilitiesParser parser = new WMTSCapabilitiesParser();
        parser.init();
        Map<String, LayerCapabilities> layers = parser.parseLayers(xml);
        System.out.println(layers.size());
    }
}
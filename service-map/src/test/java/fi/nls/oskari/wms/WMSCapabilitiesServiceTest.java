package fi.nls.oskari.wms;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.junit.Test;
import org.oskari.maplayer.model.ServiceCapabilitiesResultWMS;

import fi.nls.oskari.util.IOHelper;

public class WMSCapabilitiesServiceTest {

    @Test
    public void testLipas() throws Exception {
        String xml = IOHelper.readString(getClass().getResourceAsStream("/capabilities_lipas_1_3_0.xml"));
        WMSCapabilities caps = WMSCapabilitiesService.createCapabilities(xml);
        List<Layer> layers = WMSCapabilitiesService.getActualLayers(caps)
                .collect(Collectors.toList());
        assertEquals(176, layers.size());

        String url = "http://www.test.domain/wms";
        String version = "1.3.0";
        String user = null;
        String pwd = null;
        Set<String> systemCRSs = new HashSet<>(Arrays.asList("EPSG:3067", "EPSG:4326"));
        ServiceCapabilitiesResultWMS result = WMSCapabilitiesService.parseCapabilitiesResults(xml, url, version, user, pwd, systemCRSs);
        // Remove one because lipas_7000_huoltorakennukset appears twice for some reason
        assertEquals(175, result.getLayers().size());
    }

}

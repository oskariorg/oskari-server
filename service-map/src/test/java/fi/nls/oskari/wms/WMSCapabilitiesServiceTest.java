package fi.nls.oskari.wms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geotools.ows.wms.Layer;
import org.geotools.ows.wms.WMSCapabilities;
import org.junit.Test;
import org.oskari.maplayer.model.ServiceCapabilitiesResultWMS;

import fi.nls.oskari.util.IOHelper;

public class WMSCapabilitiesServiceTest {

    @Test
    public void testLipas() throws Exception {
        String xml = IOHelper.readString(getClass().getResourceAsStream("capabilities_lipas_1_3_0.xml"));
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

    @Test
    public void testLmi() throws Exception {
        String xml = IOHelper.readString(getClass().getResourceAsStream("capabilities_lmi_1_1_1.xml"));
        WMSCapabilities caps = WMSCapabilitiesService.createCapabilities(xml);
        List<Layer> layers = WMSCapabilitiesService.getActualLayers(caps)
                .collect(Collectors.toList());

        assertEquals(23, layers.size());
        Layer layer = layers.get(0);
        assertEquals("Expected name should match", "LMI_Island_einfalt", layer.getName());
        assertEquals("Documentary purpose for empty srs-tag handling", "[, EPSG:3857, EPSG:8088, EPSG:3057, EPSG:900913]", layer.getSrs().toString());

        String url = "http://www.test.domain/wms";
        String version = "1.1.1";
        String user = null;
        String pwd = null;
        Set<String> systemCRSs = new HashSet<>(Arrays.asList("EPSG:3067", "EPSG:4326"));
        WMSCapabilitiesService.parseCapabilitiesResults(xml, url, version, user, pwd, systemCRSs);

        /**
         * parseCapabilitiesResults() will log warnings like:
         *
         [WARN] fi.nls.oskari.wms.WMSCapabilitiesService: Couldn't parse capabilities for WMS layer: LMI_Island_einfalt message: java.lang.IllegalArgumentException: Invalid SRSName
         [WARN] fi.nls.oskari.wms.WMSCapabilitiesService: Couldn't parse capabilities for WMS layer: Bathymetry_3857 message: java.lang.IllegalArgumentException: Invalid SRSName
         [WARN] fi.nls.oskari.wms.WMSCapabilitiesService: Couldn't parse capabilities for WMS layer: AMS message: java.lang.IllegalArgumentException: Invalid SRSName
         ...
         * Because the top layer has an empty <SRS></SRS> tag.
         *
         * This test case is mostly for documentary purposes of what might go wrong...
         */
    }

}

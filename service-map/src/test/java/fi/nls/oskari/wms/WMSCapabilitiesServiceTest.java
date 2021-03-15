package fi.nls.oskari.wms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.stream.Collectors;

import fi.nls.oskari.util.PropertyUtil;
import org.geotools.ows.wms.Layer;
import org.geotools.ows.wms.WMSCapabilities;
import org.junit.Test;
import org.oskari.maplayer.model.MapLayerAdminOutput;
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
    @Test
    public void test_capabilities_json_1_3_0() throws Exception {
        String xml = IOHelper.readString(getClass().getResourceAsStream("capa_1_3_0.xml"));
        String url = "http://www.test.domain/wms";
        String version = "1.3.0";
        String user = null;
        String pwd = null;
        String name = "LayerName";
        Set<String> systemCRSs = new HashSet<>(Arrays.asList("EPSG:3067", "EPSG:4326"));
        ServiceCapabilitiesResultWMS result = WMSCapabilitiesService.parseCapabilitiesResults(xml, url, version, user, pwd, systemCRSs);
        MapLayerAdminOutput layer = result.getLayers().get(name);

        // Test info parsed for oskari layer from capabilities xml (admin add new layer)
        Map<String, Map<String, String>> locales = layer.getLocale();
        for (String lang : PropertyUtil.getSupportedLanguages()) {
            Map <String, String> locale = locales.get(lang);
            assertEquals("LayerTitle", locale.get("name"));
        }
        assertEquals("first style should be selected for layer's style", "default", layer.getStyle());
        // THIS IS ON PURPOSE: min -> max, max -> min
        assertEquals( 4724.702381, layer.getMaxscale(), 0.1);
        assertEquals( 18898809.523810, layer.getMinscale(), 0.1);

        // Test layer capabilities json (insert new layer and update capabilities)
        Map<String, Object> capa = layer.getCapabilities();
        assertTrue((boolean) capa.get("isQueryable"));
        assertEquals("AAAAA1234-1234-1234-1AAA-1A1A1A111A1", capa.get("metadataUuid"));
        assertEquals(layer.getVersion(), capa.get("version"));

        String geom = "POLYGON ((19.158138 59.756708, 19.158138 68.910986, 31.417899 68.910986, 31.417899 59.756708, 19.158138 59.756708))";
        assertEquals(geom, capa.get("geom"));

        Map <String, Object> formats = (Map <String, Object>)capa.get("formats");
        assertEquals("text/html", formats.get("value"));
        assertEquals(4, ((List)formats.get("available")).size());

        Set <String> crs = new HashSet<>((List <String>)capa.get("srs"));
        assertTrue("All and only system crs found from capabilities", crs.equals(systemCRSs));

        List<Object> styles = (List)capa.get("styles");
        assertEquals(1, styles.size());
        Map<String,String> style = (Map<String,String>)styles.get(0);
        assertEquals(layer.getStyle(), style.get("name"));
        assertEquals("LayerName", style.get("title"));
        assertEquals("http://www.test.domain/wms?request=GetLegendGraphic%26version=1.3.0%26format=image/png%26layer=LayerName", style.get("legend"));

        List<String> times = (List) capa.get("times");
        assertEquals(4, times.size());
        assertEquals("1931-12-31T00:00:00.000Z", times.get(0));
    }

}

package org.oskari.capabilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class UpdateCapabilitiesJobTest {

    private static UpdateCapabilitiesJob job;

    private static OskariLayer a = getOskariLayer(1, "A", "http://3", OskariLayer.TYPE_WMTS, "1.0.0");
    private static OskariLayer b = getOskariLayer(2, "B", "http://2", OskariLayer.TYPE_WMTS, "1.0.0");
    private static OskariLayer c = getOskariLayer(3, "C", "http://1", OskariLayer.TYPE_WMTS, "1.0.0");

    private static OskariLayer getOskariLayer(int id, String name, String url, String type, String version) {
        OskariLayer o = new OskariLayer();
        o.setId(id);
        o.setName(name);
        o.setUrl(url);
        o.setType(type);
        o.setVersion(version);
        return o;
    }

    @BeforeClass
    public static void init() {
        List<OskariLayer> layers = Arrays.asList(a, b, c);
        OskariLayerService layerService = mock(OskariLayerService.class);
        when(layerService.findAll()).thenReturn(layers);
        CapabilitiesCacheService capabilitiesService = mock(CapabilitiesCacheService.class);
        job = new UpdateCapabilitiesJob(layerService, capabilitiesService, 0);
    }

    @Test
    public void testCanUpdate() {
        assertTrue("Should allow WMTS", job.canUpdate(OskariLayer.TYPE_WMTS));
        assertFalse("Should NOT allow WMS", job.canUpdate(OskariLayer.TYPE_WMS));
        assertFalse("Should NOT allow WFS", job.canUpdate(OskariLayer.TYPE_WFS));
        assertFalse("Should NOT allow analysis", job.canUpdate(OskariLayer.TYPE_ANALYSIS));
        assertFalse("Should NOT allow argcis93", job.canUpdate(OskariLayer.TYPE_ARCGIS93));
        assertFalse("Should NOT allow stats", job.canUpdate(OskariLayer.TYPE_STATS));
        assertFalse("Should NOT allow userlayer", job.canUpdate(OskariLayer.TYPE_USERLAYER));
    }

    @Test
    public void testGetValidLayers() {
        List<OskariLayer> layers = job.getValidLayers();
        assertEquals("WFS layer should be missing", 9, layers.size());
        assertTrue(a == layers.get(0));
        assertTrue(b == layers.get(1));
        assertTrue(c == layers.get(2));
    }

    @Test
    public void testSortLayersByUrlTypeVersion() {
        List<OskariLayer> layers = job.getValidLayers();
        job.sortLayersByUrlTypeVersion(layers);
        assertTrue(c == layers.get(0));
        assertTrue(b == layers.get(1));
        assertTrue(a == layers.get(2));
    }

    @Test
    public void test() {
        String url = "https://karttamoottori.maanmittauslaitos.fi/maasto/wmts";
        String type = OskariLayer.TYPE_WMTS;
        String version = "1.0.0";
        OskariLayer orto_vaara = getOskariLayer(1, "ortokuva_vaaravari", url, type, version);
        List<OskariLayer> group = new ArrayList<>();
        group.add(orto_vaara);
        Timestamp ts = new Timestamp(System.currentTimeMillis() - 100);
        job.updateCapabilitiesGroup(url, type, version, group, ts);
    }

}

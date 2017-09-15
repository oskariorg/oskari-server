package org.oskari.capabilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import org.junit.BeforeClass;
import org.junit.Test;

public class UpdateCapabilitiesJobTest {

    private static UpdateCapabilitiesJob job;

    @BeforeClass
    public static void init() {
        OskariLayerService layerService = mock(OskariLayerService.class);
        CapabilitiesCacheService capabilitiesService = mock(CapabilitiesCacheService.class);
        job = new UpdateCapabilitiesJob(layerService, capabilitiesService, 0);
    }

    @Test
    public void testCanUpdate() {
        assertTrue("Should allow WMS", job.canUpdate(OskariLayer.TYPE_WMS));
        assertTrue("Should allow WMTS", job.canUpdate(OskariLayer.TYPE_WMTS));
        assertFalse("Should NOT allow WFS", job.canUpdate(OskariLayer.TYPE_WFS));
        assertFalse("Should NOT allow analysis", job.canUpdate(OskariLayer.TYPE_ANALYSIS));
        assertFalse("Should NOT allow argcis93", job.canUpdate(OskariLayer.TYPE_ARCGIS93));
        assertFalse("Should NOT allow stats", job.canUpdate(OskariLayer.TYPE_STATS));
        assertFalse("Should NOT allow userlayer", job.canUpdate(OskariLayer.TYPE_USERLAYER));
    }

}

package org.oskari.capabilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.nls.oskari.domain.map.OskariLayer;

public class CapabilitiesUpdateServiceTest {

    @Test
    public void testCanUpdate() {
        assertTrue("Should allow WMTS", CapabilitiesUpdateService.canUpdate(OskariLayer.TYPE_WMTS));
        assertTrue("Should allow WMS", CapabilitiesUpdateService.canUpdate(OskariLayer.TYPE_WMS));
        assertFalse("Should NOT allow WFS", CapabilitiesUpdateService.canUpdate(OskariLayer.TYPE_WFS));
        assertFalse("Should NOT allow analysis", CapabilitiesUpdateService.canUpdate(OskariLayer.TYPE_ANALYSIS));
        assertFalse("Should NOT allow argcis93", CapabilitiesUpdateService.canUpdate(OskariLayer.TYPE_ARCGIS93));
        assertFalse("Should NOT allow stats", CapabilitiesUpdateService.canUpdate(OskariLayer.TYPE_STATS));
        assertFalse("Should NOT allow userlayer", CapabilitiesUpdateService.canUpdate(OskariLayer.TYPE_USERLAYER));
    }

}

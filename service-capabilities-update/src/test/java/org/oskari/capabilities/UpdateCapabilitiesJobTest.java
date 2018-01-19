package org.oskari.capabilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.nls.oskari.domain.map.OskariLayer;

public class UpdateCapabilitiesJobTest {

    @Test
    public void testCanUpdate() {
        assertTrue("Should allow WMTS", UpdateCapabilitiesJob.canUpdate(OskariLayer.TYPE_WMTS));
        assertTrue("Should allow WMS", UpdateCapabilitiesJob.canUpdate(OskariLayer.TYPE_WMS));
        assertFalse("Should NOT allow WFS", UpdateCapabilitiesJob.canUpdate(OskariLayer.TYPE_WFS));
        assertFalse("Should NOT allow analysis", UpdateCapabilitiesJob.canUpdate(OskariLayer.TYPE_ANALYSIS));
        assertFalse("Should NOT allow argcis93", UpdateCapabilitiesJob.canUpdate(OskariLayer.TYPE_ARCGIS93));
        assertFalse("Should NOT allow stats", UpdateCapabilitiesJob.canUpdate(OskariLayer.TYPE_STATS));
        assertFalse("Should NOT allow userlayer", UpdateCapabilitiesJob.canUpdate(OskariLayer.TYPE_USERLAYER));
    }

}

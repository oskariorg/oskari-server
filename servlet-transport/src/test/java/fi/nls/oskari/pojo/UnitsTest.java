package fi.nls.oskari.pojo;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class UnitsTest {
    @Test
    public void testGetScalesInSrs() {
        Units units = new Units();
        double fromScale = 2000.0;
        double toScale;

        toScale = units.getScaleInSrs(fromScale, "EPSG:3067", "EPSG:3067");
        assertTrue("Scales should have the same value", fromScale == toScale);

        // From meters to meters
        toScale = units.getScaleInSrs(fromScale, "EPSG:3067", "EPSG:3035");
        assertTrue("Scale should have expected value", toScale == fromScale);

        // From meters to US feet
        toScale = units.getScaleInSrs(fromScale, "EPSG:3067", "EPSG:2263");
        assertTrue("Scale should have expected value", toScale == 609.6);

        // From meters to degrees
        toScale = units.getScaleInSrs(fromScale, "EPSG:3067", "EPSG:4326");
        assertTrue("Scale should have expected value", toScale == 34.906585039886565);

        toScale = units.getScaleInSrs(fromScale, "EPSG:3067", "EPSG:10");
        assertTrue("Should return the given scale for unknown SRS name", toScale == fromScale);
    }
}

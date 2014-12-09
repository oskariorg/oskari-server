package fi.nls.oskari.map.geometry;

import fi.nls.oskari.domain.geo.Point;
import org.geotools.referencing.CRS;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Simple test cases for projection transforms
 */
public class ProjectionHelperTest {
    private String src = "EPSG:4258";
    private String target = "EPSG:3067";

    @Test
    public void testTransformPoint() throws Exception {
        double x = 60.113924;
        double y = 25.017104;

        Point p2 = ProjectionHelper.transformPoint(new Point(x, y), src, target);
        assertEquals("lon", 389790.2122344108, p2.getLon(), 0.0);
        assertEquals("lat", 6665752.471279182, p2.getLat(), 0.0);

        Point p3 = ProjectionHelper.transformPoint(x, y, src, target);
        assertEquals("lon", 389790.2122344108, p3.getLon(), 0.0);
        assertEquals("lat", 6665752.471279182, p3.getLat(), 0.0);
        /*
        16:25:15,235 DEBUG [ELFGeoLocatorParser:39] Original coordinates - x: 60.113924 y: 25.017104
        16:25:15,235 DEBUG [ELFGeoLocatorParser:39] Transformed coordinates - x: 389790.2122344108 y: 6665752.471279182
        */
    }

    @Test
    public void testIsFirstAxisNorth() throws Exception {

        assertTrue("First axis north " + src, ProjectionHelper.isFirstAxisNorth(CRS.decode(src)));
        assertTrue("First axis north " + WKTHelper.PROJ_EPSG_4326, ProjectionHelper.isFirstAxisNorth(CRS.decode(WKTHelper.PROJ_EPSG_4326)));
        assertFalse("First axis NOT north " + target, ProjectionHelper.isFirstAxisNorth(CRS.decode(target)));
    }
}

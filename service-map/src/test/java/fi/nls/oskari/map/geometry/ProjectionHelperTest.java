package fi.nls.oskari.map.geometry;

import fi.nls.oskari.domain.geo.Point;
import org.geotools.referencing.CRS;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simple test cases for projection transforms
 */
public class ProjectionHelperTest {
    private String EPSG_4258 = "EPSG:4258";
    private String EPSG_3067 = "EPSG:3067";
    private String EPSG_4326 = "EPSG:4326";
    private String EPSG_3575 = "EPSG:3575";

    @Test
    public void testTransformPoint() throws Exception {
        double x = 60.113924;
        double y = 25.017104;

        Point p2 = ProjectionHelper.transformPoint(new Point(x, y), EPSG_4258, EPSG_3067);
        assertEquals("lon", 389790.2122344108, p2.getLon(), 0.0);
        assertEquals("lat", 6665752.471279182, p2.getLat(), 0.0);

        Point p3 = ProjectionHelper.transformPoint(x, y, EPSG_4258, EPSG_3067);
        assertEquals("lon", 389790.2122344108, p3.getLon(), 0.0);
        assertEquals("lat", 6665752.471279182, p3.getLat(), 0.0);
        /*
        16:25:15,235 DEBUG [ELFGeoLocatorParser:39] Original coordinates - x: 60.113924 y: 25.017104
        16:25:15,235 DEBUG [ELFGeoLocatorParser:39] Transformed coordinates - x: 389790.2122344108 y: 6665752.471279182
        */
    }

    @Test
    public void testTransformPointEPSG_4326NoTransform() throws Exception {
        double lat = 61.4980214;
        double lon = 23.7603118;

        Point p3 = ProjectionHelper.transformPoint(lon, lat, EPSG_4326, EPSG_4326);
        assertEquals("lon", lon, p3.getLon(), 0.0);
        assertEquals("lat", lat, p3.getLat(), 0.0);
    }

    @Test
    public void testTransformPointEPSG_4326ToEPSG_3067() throws Exception {
        //double lat = 33.02221;
        //double lon = -179.99;

        double lat = 33.02221;
        double lon = -90.0;

        Point p3 = ProjectionHelper.transformPoint(lat, lon, EPSG_4326, EPSG_3575);


        double lat2 = 33.02221;
        double lon2 = 90;

        Point p4 = ProjectionHelper.transformPoint(lat2, lon2, EPSG_4326, EPSG_3575);

    }

    @Test
    public void testIsFirstAxisNorth() throws Exception {

        assertTrue("First axis north " + EPSG_4258, ProjectionHelper.isFirstAxisNorth(CRS.decode(EPSG_4258)));
        assertTrue("First axis north " + WKTHelper.PROJ_EPSG_4326, ProjectionHelper.isFirstAxisNorth(CRS.decode(WKTHelper.PROJ_EPSG_4326)));
        assertFalse("First axis NOT north " + EPSG_3067, ProjectionHelper.isFirstAxisNorth(CRS.decode(EPSG_3067)));
    }
}

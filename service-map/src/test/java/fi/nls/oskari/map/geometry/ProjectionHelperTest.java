package fi.nls.oskari.map.geometry;

import fi.nls.oskari.domain.geo.Point;
import org.geotools.referencing.CRS;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.junit.Assert.*;

/**
 * Simple test cases for projection transforms
 */
public class ProjectionHelperTest {
    private String EPSG_4258 = "EPSG:4258";
    private String EPSG_3067 = "EPSG:3067";
    private String EPSG_4326 = "EPSG:4326";
    private String EPSG_3575 = "EPSG:3575";
    private String EPSG_3035 = "EPSG:3035";
    private String EPSG_3857 = "EPSG:3857";
    private String EPSG_3878 = "EPSG:3878";

    /*
    For to test fi.nls.oskari.NLSFIPointTransformer, add below dependency to control-base
     <dependency>
            <groupId>fi.nls.oskari.extras</groupId>
            <artifactId>nlsfi-projections</artifactId>
            <version>1.1</version>
        </dependency>
     */
    private static final String className ="fi.nls.oskari.NLSFIPointTransformer";
    private static PointTransformer service = null;

    @BeforeClass
    public static void setup() {

        try {
            final Class clazz = Class.forName(className);
            service = (PointTransformer) clazz.newInstance();
        } catch (Exception e) {
            System.err.println("Error initalizing projection library for classname: " + className +
                    " - Make sure it's available in the classpath.");
        }
        if (service == null) {
            service = new DefaultPointTransformer();
        }
    }

    @Test
    public void testTransformPoint() throws Exception {
        double y = 60.113924;
        double x = 25.017104;

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

    //@Test
    public void testTransformPointEPSG_4326ToEPSG_3067() throws Exception {

        double lat = 33.02221;
        double lon = -90.0;

        Point p3 = ProjectionHelper.transformPoint(lon, lat, EPSG_4326, EPSG_3067);

       // input lon,lat out of 3067 bounds
       //  West Bound Longitude: 19.1
       //  South Bound Latitude: 58.8
       //  East Bound Longitude: 31.6
       //  North Bound Latitude: 70.1

        assertEquals("Point 4326->3067", p3, null); // lon,lat out of 3067 bounds West Bound Longitude: 19.1
    }

    @Test
    public void testTransformPointEPSG_3067ToEPSG_4326() throws Exception {


        double lat = 7596848.323;
        double lon = 500001.1369;

        Point p3 = ProjectionHelper.transformPoint( lon, lat, EPSG_3067,EPSG_4326);


        Point p4 = ProjectionHelper.transformPoint( p3.getLon(), p3.getLat(), EPSG_4326, EPSG_3067);
        assertEquals("lon 3067-45326-3067", lon, p4.getLon(), 0.01); // unit m, tolerance 1 cm
        assertEquals("lat 3067-4326-3067", lat, p4.getLat(), 0.01);
    }

    @Test
    public void testTransformPointCRSsForceXY() throws Exception {
        //double lat = 33.02221;
        //double lon = -179.99;

        double lat = 61.49802149449156;
        double lon = 23.7603118;
        CoordinateReferenceSystem CRS4326 = CRS.decode(EPSG_4326, true); //ne
        CoordinateReferenceSystem CRS3035 = CRS.decode(EPSG_3035, true); //ne
        CoordinateReferenceSystem CRS3067 = CRS.decode(EPSG_3067, true);  //en
        CoordinateReferenceSystem CRS3857 = CRS.decode(EPSG_3857, true);  //en


        Point p3 = ProjectionHelper.transformPoint(lon, lat, CRS4326, CRS3035);
        //System.out.println("4326to3035 " + p3.toString());


        Point p4 = ProjectionHelper.transformPoint(p3.getLon(), p3.getLat(), CRS3035, CRS4326);
        //System.out.println("3035to4326 " + p4.toString());

        assertEquals("lon 3035to4326", lon, p4.getLon(), 0.000001);  // unit degree
        assertEquals("lat 3035to4326", lat, p4.getLat(), 0.000001);

        Point p5 = ProjectionHelper.transformPoint(p4.getLon(), p4.getLat(), CRS4326, CRS3067);
        //System.out.println("4326to3067 " + p5.toString());

        Point p6 = ProjectionHelper.transformPoint(p5.getLon(), p5.getLat(), CRS3067, CRS4326);
        //System.out.println("3067to4326 " + p6.toString());

        assertEquals("lon 3067to4326", lon, p6.getLon(), 0.000001); // unit degree
        assertEquals("lat 3067to4326", lat, p6.getLat(), 0.000001);

        Point p7 = ProjectionHelper.transformPoint(p5.getLon(), p5.getLat(), CRS3067, CRS3857);
        //System.out.println("3067to3857 " + p7.toString());

        Point p8 = ProjectionHelper.transformPoint(p7.getLon(), p7.getLat(), CRS3857, CRS3067);
        //System.out.println("3857to3067 " + p8.toString());

        assertEquals("lon 3857to3067", p5.getLon(), p8.getLon(), 0.01); // unit m, tolerance 1 cm
        assertEquals("lat 3857to3067", p5.getLat(), p8.getLat(), 0.01);

    }

    @Test
    public void testTransformPointCRSs() throws Exception {
        //double lat = 33.02221;
        //double lon = -179.99;

        double lat = 61.49802149449156;
        double lon = 23.7603118;
        CoordinateReferenceSystem CRS4326 = CRS.decode(EPSG_4326); //ne
        CoordinateReferenceSystem CRS3035 = CRS.decode(EPSG_3035); //ne
        CoordinateReferenceSystem CRS3067 = CRS.decode(EPSG_3067);  //en
        CoordinateReferenceSystem CRS3857 = CRS.decode(EPSG_3857);  //en


        Point p3 = ProjectionHelper.transformPoint(lon, lat, CRS4326, CRS3035);
        //System.out.println("4326to3035 " + p3.toString());


        Point p4 = ProjectionHelper.transformPoint(p3.getLon(), p3.getLat(), CRS3035, CRS4326);
        //System.out.println("3035to4326 " + p4.toString());

        assertEquals("lon 3035to4326", lon, p4.getLon(), 0.000001);  // unit degree
        assertEquals("lat 3035to4326", lat, p4.getLat(), 0.000001);

        Point p5 = ProjectionHelper.transformPoint(p4.getLon(), p4.getLat(), CRS4326, CRS3067);
        //System.out.println("4326to3067 " + p5.toString());

        Point p6 = ProjectionHelper.transformPoint(p5.getLon(), p5.getLat(), CRS3067, CRS4326);
        //System.out.println("3067to4326 " + p6.toString());

        assertEquals("lon 3067to4326", lon, p6.getLon(), 0.000001); // unit degree
        assertEquals("lat 3067to4326", lat, p6.getLat(), 0.000001);

        Point p7 = ProjectionHelper.transformPoint(p5.getLon(), p5.getLat(), CRS3067, CRS3857);
        //System.out.println("3067to3857 " + p7.toString());

        Point p8 = ProjectionHelper.transformPoint(p7.getLon(), p7.getLat(), CRS3857, CRS3067);
        //System.out.println("3857to3067 " + p8.toString());

        assertEquals("lon 3857to3067", p5.getLon(), p8.getLon(), 0.01); // unit m, tolerance 1 cm
        assertEquals("lat 3857to3067", p5.getLat(), p8.getLat(), 0.01);

    }

    @Test
    public void testReprojectPointCRSs() throws Exception {
        //Lahti city
        double lat = 6760528;
        double lon = 428128;

        Point p1 = new Point(lon, lat);
        Point p2 = service.reproject(p1, EPSG_3067, EPSG_4258);
        Point p3 = service.reproject(p2, EPSG_4258, EPSG_3878);

        Point p4 = service.reproject(p3, EPSG_3878, EPSG_4258);
        assertEquals("lon 3878to4258", p2.getLon(), p4.getLon(), 0.000001);  // unit degree
        assertEquals("lat 3878to4258", p2.getLat(), p4.getLat(), 0.000001);

        Point p5 = service.reproject(p4, EPSG_4258, EPSG_3067);

        assertEquals("lon 3067to3067", lon, p5.getLon(), 0.01); // unit degree
        assertEquals("lat 3067to3067", lat, p5.getLat(), 0.01);

        Point p7 = service.reproject(p5, EPSG_3067, EPSG_3878);
        assertEquals("lon 3067to3878", p3.getLon(), p7.getLon(), 0.01); // unit m, tolerance 1 cm
        assertEquals("lat 3067to3878", p3.getLat(), p7.getLat(), 0.01);

        Point p8 = service.reproject(p7, EPSG_3878, EPSG_3067);

        assertEquals("lon 3878to3067", p1.getLon(), p8.getLon(), 0.01); // unit m, tolerance 1 cm
        assertEquals("lat 3878to3067", p1.getLat(), p8.getLat(), 0.01);

    }


    @Test
    public void testIsFirstAxisNorth() throws Exception {

        assertTrue("First axis north " + EPSG_4258, ProjectionHelper.isFirstAxisNorth(CRS.decode(EPSG_4258)));
        assertTrue("First axis north " + WKTHelper.PROJ_EPSG_4326, ProjectionHelper.isFirstAxisNorth(CRS.decode(WKTHelper.PROJ_EPSG_4326)));
        assertFalse("First axis NOT north " + EPSG_3067, ProjectionHelper.isFirstAxisNorth(CRS.decode(EPSG_3067)));
    }
}

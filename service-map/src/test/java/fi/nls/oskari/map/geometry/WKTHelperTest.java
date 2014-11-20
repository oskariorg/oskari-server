package fi.nls.oskari.map.geometry;

import com.vividsolutions.jts.geom.Geometry;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WKTHelperTest {

    final String PROJ_INVALID = "asdf & qwerty";

    final String WKT_POINT = "POINT (30 10)";
    final String WKT_POLYGON = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";

    @Test
    public void testGetCRS() throws Exception {
        CoordinateReferenceSystem crs = WKTHelper.getCRS(WKTHelper.PROJ_EPSG_4326);
        assertTrue("Got crs for proj " + WKTHelper.PROJ_EPSG_4326, crs != null);

        crs = WKTHelper.getCRS(WKTHelper.PROJ_EPSG_3067);
        assertTrue("Got crs for proj " + WKTHelper.PROJ_EPSG_3067, crs != null);

        crs = WKTHelper.getCRS(PROJ_INVALID);
        assertTrue("Null crs for invalid proj " + PROJ_INVALID, crs == null);
    }

    @Test
    public void testParseWKT() throws Exception {
        Geometry point = WKTHelper.parseWKT(WKT_POINT);

        assertTrue("Got point from " + WKT_POINT, point != null);
        assertTrue("Point x correct", point.getCoordinate().x == 30);
        assertTrue("Point y correct", point.getCoordinate().y == 10);

        Geometry polygon = WKTHelper.parseWKT(WKT_POLYGON);
        assertTrue("Got polygon from " + WKT_POLYGON, polygon != null);
    }

    @Test
    public void testTransform() throws Exception {
        Geometry point = WKTHelper.parseWKT("POINT (60.170014 24.938466)");
        Geometry transformed = WKTHelper.transform(point, WKTHelper.PROJ_EPSG_4326, WKTHelper.PROJ_EPSG_3067);
        assertTrue("Got point from " + transformed, transformed != null);
        assertTrue("Point y correct " + transformed.getCoordinate().y, transformed.getCoordinate().y == 6672130.961068579);
        assertTrue("Point x correct " + transformed.getCoordinate().x, transformed.getCoordinate().x == 385615.3743527662);
    }

    @Test
    public void testGetWKT() throws Exception {
        Geometry point = WKTHelper.parseWKT(WKT_POINT);
        final String str = WKTHelper.getWKT(point);
        assertEquals("Should have same WKT", WKT_POINT, str);
    }


}

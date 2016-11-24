package fi.nls.oskari.map.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.domain.geo.Point;
import org.geotools.referencing.CRS;
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
        Geometry point = WKTHelper.parseWKT("POINT (24.938466 60.170014)");
        Geometry transformed = WKTHelper.transform(point, WKTHelper.PROJ_EPSG_4326, WKTHelper.PROJ_EPSG_3067);
        assertTrue("Got point from " + transformed, transformed != null);
        assertTrue("Point y correct " + transformed.getCoordinate().y, transformed.getCoordinate().y == 6672130.961068579);
        assertTrue("Point x correct " + transformed.getCoordinate().x, transformed.getCoordinate().x == 385615.3743527662);
    }

    @Test
    public void testCoverageTransform() throws Exception {
        String wkt = "POLYGON ((19.08317359 59.45414258, 19.08317359 70.09229553, 31.58672881 70.09229553, 31.58672881 59.45414258, 19.08317359 59.45414258))";
        String transformed = WKTHelper.transformLayerCoverage(wkt, WKTHelper.PROJ_EPSG_3067);

        assertTrue("Got result: " + transformed, transformed != null);
        String expected = "POLYGON ((51857.07752019336 6617351.758085947, 199877.68034737493 7795699.644448195, 674163.706166442 7782724.690696563, 759905.4330615391 6599589.560233721, 51857.07752019336 6617351.758085947))";
        //was earlier in reversed order ??  "POLYGON ((51857.07752019336 6617351.758085947, 759905.4330615391 6599589.560233721, 674163.706166442 7782724.690696563, 199877.68034737493 7795699.644448195, 51857.07752019336 6617351.758085947))";
        assertEquals("Polygon epsg:3067  correct ", expected, transformed);
    }


    @Test
    public void testGetWKT() throws Exception {
        Geometry point = WKTHelper.parseWKT(WKT_POINT);
        final String str = WKTHelper.getWKT(point);
        assertEquals("Should have same WKT", WKT_POINT, str);
    }


}

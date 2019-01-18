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
        Geometry point = WKTHelper.parseWKT("POINT (24.938466 60.170014)");
        Geometry transformed = WKTHelper.transform(point, WKTHelper.PROJ_EPSG_4326, WKTHelper.PROJ_EPSG_3067);
        assertTrue("Got point from " + transformed, transformed != null);
        assertTrue("Point y correct " + transformed.getCoordinate().y, transformed.getCoordinate().y == 6672130.96106858);
        assertTrue("Point x correct " + transformed.getCoordinate().x, transformed.getCoordinate().x == 385615.37435276626);
    }

    @Test
    public void testCoverageTransform() throws Exception {
        String wkt = "POLYGON ((19.08317359 59.45414258, 19.08317359 70.09229553, 31.58672881 70.09229553, 31.58672881 59.45414258, 19.08317359 59.45414258))";
        String transformed = WKTHelper.transformLayerCoverage(wkt, WKTHelper.PROJ_EPSG_3067);

        assertTrue("Got result: " + transformed, transformed != null);
        String expected = "POLYGON ((51857.07752019371 6617351.758085947, 64757.517549807904 6724539.180098944, 77780.43746099574 6831712.629646971, 90922.13308078656 6938872.410219978, 104178.8701831837 7046018.839966642, 117546.8855180841 7153152.251308998, 131022.38783961337 7260272.990545033, 144601.5589337572 7367381.417439794, 158280.55464517837 7474477.904805447, 172055.50590313738 7581562.838070868, 185922.51974644425 7688636.614841255, 199877.68034737493 7795699.644448195, 236191.67830601014 7791243.318616742, 272563.0624726218 7787362.586186218, 308983.85680056794 7784057.789610645, 345446.1030066917 7781329.218095038, 381941.85780175345 7779177.108736253, 418463.1900783633 7777601.647466223, 455002.1780645468 7776602.969796763, 491550.9064510595 7776181.161365283, 528101.4635005761 7776336.258280902, 564645.9381468829 7777068.247270706, 601176.4170921873 7778377.065625957, 637684.9819106762 7780262.600948358, 674163.7061664416 7782724.690696563, 682253.5601090215 7675142.326825145, 690291.1725247316 7567564.430475263, 698274.2458524777 7459991.165674922, 706200.4989268244 7352422.690918106, 714067.6676445409 7244859.158983922, 721873.5056238829 7137300.716763378, 729615.7848563927 7029747.505094045, 737292.2963510038 6922199.658602795, 744900.8507702622 6814657.30555671, 752439.279058465 6707120.567722379, 759905.4330615384 6599589.560233721, 705444.5950397715 6596222.67797946, 650955.6845310468 6593645.009427913, 596446.183832961 6591856.154975107, 541923.5442930857 6590855.836400321, 487395.19748305867 6590643.897801558, 432868.56637780176 6591220.306118018, 378351.0765369696 6592585.151240729, 323850.1672857235 6594738.6457116045, 269373.3028919413 6597681.12401024, 214927.98373695806 6601413.041426821, 160521.75747694494 6605934.9725185605, 106162.23019201815 6611247.60914618, 51857.07752019371 6617351.758085947))";
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

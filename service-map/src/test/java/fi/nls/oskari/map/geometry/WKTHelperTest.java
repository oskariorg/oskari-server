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
        assertTrue("Point y correct " + transformed.getCoordinate().y, transformed.getCoordinate().y == 6672130.961068579);
        assertTrue("Point x correct " + transformed.getCoordinate().x, transformed.getCoordinate().x == 385615.3743527662);
    }

    @Test
    public void testCoverageTransform() throws Exception {
        String wkt = "POLYGON ((19.08317359 59.45414258, 19.08317359 70.09229553, 31.58672881 70.09229553, 31.58672881 59.45414258, 19.08317359 59.45414258))";
        String transformed = WKTHelper.transformLayerCoverage(wkt, WKTHelper.PROJ_EPSG_3067);

        assertTrue("Got result: " + transformed, transformed != null);
        String expected = "POLYGON ((51857.07752019336 6617351.758085947, 64757.51754980773 6724539.1800989425, 77780.43746099574 6831712.629646971, 90922.13308078656 6938872.410219978, 104178.87018318352 7046018.839966641, 117546.88551808393 7153152.251308996, 131022.38783961348 7260272.990545034, 144601.5589337572 7367381.417439794, 158280.5546451786 7474477.904805448, 172055.50590313738 7581562.838070868, 185922.51974644407 7688636.614841255, 199877.68034737493 7795699.644448195, 236191.67830600997 7791243.318616742, 272563.0624726217 7787362.586186218, 308983.85680056794 7784057.789610645, 345446.10300669156 7781329.218095038, 381941.8578017532 7779177.108736253, 418463.190078363 7777601.647466223, 455002.17806454666 7776602.969796763, 491550.9064510594 7776181.161365283, 528101.4635005761 7776336.258280902, 564645.9381468832 7777068.247270706, 601176.4170921873 7778377.065625957, 637684.9819106762 7780262.600948358, 674163.706166442 7782724.690696563, 682253.5601090218 7675142.326825145, 690291.1725247321 7567564.430475263, 698274.2458524778 7459991.165674924, 706200.4989268251 7352422.690918106, 714067.6676445415 7244859.158983923, 721873.5056238836 7137300.716763376, 729615.7848563935 7029747.505094043, 737292.2963510042 6922199.658602795, 744900.8507702629 6814657.30555671, 752439.279058465 6707120.567722377, 759905.4330615391 6599589.560233721, 705444.5950397715 6596222.67797946, 650955.6845310468 6593645.009427913, 596446.1838329614 6591856.154975107, 541923.5442930857 6590855.836400321, 487395.1974830585 6590643.897801558, 432868.5663778016 6591220.306118018, 378351.0765369692 6592585.151240729, 323850.167285723 6594738.6457116045, 269373.30289194116 6597681.12401024, 214927.98373695806 6601413.041426821, 160521.75747694494 6605934.9725185605, 106162.23019201797 6611247.60914618, 51857.07752019336 6617351.758085947))";
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

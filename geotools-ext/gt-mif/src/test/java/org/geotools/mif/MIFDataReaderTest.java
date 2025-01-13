package org.geotools.mif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class MIFDataReaderTest {

    private static final double EPSILON = 1e-7;

    @Test
    public void testPlineWithNumtsOnSameLine() throws URISyntaxException, IOException {
        File mif = new File(getClass().getResource("X51_SahkoLinja.mif").toURI());

        LineString pline1;
        LineString pline2;
        LineString pline3;
        try (MIFDataReader r = new MIFDataReader(mif)) {
            pline1 = (LineString) r.next();
            pline2 = (LineString) r.next();
            pline3 = (LineString) r.next();
        }

        Assertions.assertEquals(53, pline1.getNumPoints());
        Assertions.assertEquals(EPSILON, 508688.045709824, pline1.getCoordinateN(0).getX());
        Assertions.assertEquals(EPSILON, 7722000.000000000, pline1.getCoordinateN(0).getY());
        Assertions.assertEquals(EPSILON, 508742.876, pline1.getCoordinateN(52).getX());
        Assertions.assertEquals(EPSILON, 7748438.773, pline1.getCoordinateN(52).getY());

        Assertions.assertEquals(30, pline2.getNumPoints());
        Assertions.assertEquals(EPSILON, 508742.876, pline2.getCoordinateN(0).getX());
        Assertions.assertEquals(EPSILON, 7748438.773, pline2.getCoordinateN(0).getY());
        Assertions.assertEquals(EPSILON, 503414.898000001, pline2.getCoordinateN(29).getX());
        Assertions.assertEquals(EPSILON, 7755431.873800000, pline2.getCoordinateN(29).getY());

        Assertions.assertEquals(39, pline3.getNumPoints());
        Assertions.assertEquals(EPSILON, 508742.876, pline3.getCoordinateN(0).getX());
        Assertions.assertEquals(EPSILON, 7748438.773, pline3.getCoordinateN(0).getY());
        Assertions.assertEquals(EPSILON, 535262.676499178, pline3.getCoordinateN(38).getX());
        Assertions.assertEquals(EPSILON, 7770000.000000000, pline3.getCoordinateN(38).getY());
    }

    @Test
    public void testLine() throws URISyntaxException, IOException {
        File mif = new File(getClass().getResource("L41_SahkoLinja.mif").toURI());
        try (MIFDataReader r = new MIFDataReader(mif)) {
            while (r.hasNext()) {
                Assertions.assertNotNull(r.next());
            }
        }
    }

}

package org.geotools.mif;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;
import org.locationtech.jts.geom.LineString;

public class MIFDataReaderTest {

    private static final double EPSILON = 1e-7;

    @Test
    public void testPlineWithNumtsOnSameLine() throws URISyntaxException, IOException {
        File mif = new File(getClass().getResource("X51_SahkoLinja.mif").toURI());

        LineString pline;
        try (MIFDataReader r = new MIFDataReader(mif)) {
            pline = (LineString) r.next();
        }

        assertEquals(53, pline.getNumPoints());
        assertEquals(EPSILON, 508688.045709824, pline.getCoordinateN(0).getX());
        assertEquals(EPSILON, 7722000.00000000, pline.getCoordinateN(0).getY());

        assertEquals(EPSILON, 508742.8760, pline.getCoordinateN(52).getX());
        assertEquals(EPSILON, 7748438.773, pline.getCoordinateN(52).getY());
    }

}

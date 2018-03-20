package fi.nls.oskari.map.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.junit.Test;

public class GeometryHelperTest {

    @Test
    public void testInterpolate() {
        GeometryFactory gf = new GeometryFactory();
        CoordinateSequence cs = gf.getCoordinateSequenceFactory().create(2, 2);
        cs.setOrdinate(0, 0, -180.0);
        cs.setOrdinate(0, 1,   45.0);
        cs.setOrdinate(1, 0,  180.0);
        cs.setOrdinate(1, 1,   45.0);
        CoordinateSequence interpolated = GeometryHelper.interpolateLinear(
                gf.createLineString(cs), 20.0, gf);

        double lon = -180.0;
        double lat =   45.0;
        for (int i = 0; i <= 18; i++) {
            assertEquals(lon, interpolated.getOrdinate(i, 0), 0.0);
            assertEquals(lat, interpolated.getOrdinate(i, 1), 0.0);
            lon += 20.0;
        }
    }

    @Test
    public void testIsWithin() {
        GeometryFactory gf = new GeometryFactory();
        CoordinateSequence cs = gf.getCoordinateSequenceFactory().create(2, 2);
        cs.setOrdinate(0, 0, -180.0);
        cs.setOrdinate(0, 1,  -45.0);
        cs.setOrdinate(1, 0,  180.0);
        cs.setOrdinate(1, 1,   45.0);

        assertTrue("Happy", GeometryHelper.isWithin(cs,  -180.0, -45.0, 180.0,  45.0));
        assertFalse("< minX", GeometryHelper.isWithin(cs, -160.0, -45.0, 180.0,  45.0));
        assertFalse("> maxX", GeometryHelper.isWithin(cs, -180.0, -45.0, 130.0,  45.0));
        assertFalse("< minY", GeometryHelper.isWithin(cs, -180.0,  20.0, 180.0,  45.0));
        assertFalse("> maxY", GeometryHelper.isWithin(cs, -180.0, -45.0, 180.0,   5.0));

        try {
            GeometryHelper.isWithin(cs, 2, 5, 1, 6);
            fail();
        } catch (IllegalArgumentException ignore) {
            assertEquals("maxX < minX", ignore.getMessage());
        }
        try {
            GeometryHelper.isWithin(cs, 1, -5, 2, -7);
            fail();
        } catch (IllegalArgumentException ignore) {
            assertEquals("maxY < minY", ignore.getMessage());
        }
    }
}

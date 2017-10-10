package fi.nls.oskari.map.geometry;

import static org.junit.Assert.assertEquals;

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

}

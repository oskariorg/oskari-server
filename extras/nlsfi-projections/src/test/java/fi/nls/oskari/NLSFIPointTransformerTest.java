package fi.nls.oskari;

import fi.nls.oskari.domain.geo.Point;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 14.3.2016.
 */
public class NLSFIPointTransformerTest {

    NLSFIPointTransformer transformer = new NLSFIPointTransformer();
    @Test
    public void testReproject()
            throws Exception {

        Point input = new Point(3632299.672, 7163325.996);
        Point value = transformer.reproject(input, "NLSFI:ykj", "NLSFI:euref");

        assertEquals("lon", 632073.8229999975, value.getLon(), 0.0);
        assertEquals("lat", 7160328.224999596, value.getLat(), 0.0);
    }

    @Test
    public void testReprojectCoordinateOrder()
            throws Exception {

        Point input = new Point(1530012.833,6942456.165);
        Point value = transformer.reproject(input, "NLSFI:kkj", "LATLON:kkj");
        System.out.println(value.getLon() + "\t" + value.getLat());
        assertEquals("lon", 21.58399205221266, value.getLon(), 0.0);
        assertEquals("lat", 62.58520521458717, value.getLat(), 0.0);
    }
}
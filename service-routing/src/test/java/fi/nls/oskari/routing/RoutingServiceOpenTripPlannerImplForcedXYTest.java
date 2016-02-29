package fi.nls.oskari.routing;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * Created by Marko Kuosmanen on 8.10.2015.
 */
public class RoutingServiceOpenTripPlannerImplForcedXYTest extends RoutingServiceOpenTripPlannerImplTest {

    @BeforeClass
    public static void initialize() throws Exception{
        PropertyUtil.addProperty("routing.srs", ROUTING_SRS);
        System.setProperty("org.geotools.referencing.forceXY", "true");
        PropertyUtil.addProperty("routing.forceXY", "true");
    }

    @AfterClass
    public static void goAway() throws Exception{
        System.clearProperty("org.geotools.referencing.forceXY");
        PropertyUtil.clearProperties();
    }
}

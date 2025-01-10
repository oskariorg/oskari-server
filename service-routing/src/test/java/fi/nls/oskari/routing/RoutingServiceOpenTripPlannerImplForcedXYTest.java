package fi.nls.oskari.routing;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Created by Marko Kuosmanen on 8.10.2015.
 */
public class RoutingServiceOpenTripPlannerImplForcedXYTest extends RoutingServiceOpenTripPlannerImplTest {

    @BeforeAll
    public static void initialize() throws Exception{
        PropertyUtil.addProperty("routing.srs", ROUTING_SRS);
        System.setProperty("org.geotools.referencing.forceXY", "true");
        PropertyUtil.addProperty("routing.forceXY", "true");
    }

    @AfterAll
    public static void goAway() throws Exception{
        System.clearProperty("org.geotools.referencing.forceXY");
        PropertyUtil.clearProperties();
    }
}

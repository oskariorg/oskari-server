package fi.nls.oskari.routing;

import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Created by Marko Kuosmanen on 8.10.2015.
 */
public class RoutingServiceOpenTripPlannerImplForcedXYTest extends RoutingServiceOpenTripPlannerImplTest {

    @BeforeEach
    public void initialize() throws Exception{
        PropertyUtil.addProperty("routing.srs", ROUTING_SRS);
        System.setProperty("org.geotools.referencing.forceXY", "true");
        PropertyUtil.addProperty("routing.forceXY", "true");
    }

    @AfterEach
    public void goAway() throws Exception{
        System.clearProperty("org.geotools.referencing.forceXY");
        PropertyUtil.clearProperties();
    }
}

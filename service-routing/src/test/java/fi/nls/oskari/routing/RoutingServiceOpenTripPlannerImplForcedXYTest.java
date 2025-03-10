package fi.nls.oskari.routing;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.HttpURLConnection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Created by Marko Kuosmanen on 8.10.2015.
 */
public class RoutingServiceOpenTripPlannerImplForcedXYTest extends RoutingServiceOpenTripPlannerImplTest {

    @BeforeEach
    public void initialize() throws Exception{
        PropertyUtil.addProperty("routing.srs", ROUTING_SRS);
        PropertyUtil.addProperty("routing.url", ROUTING_URL);
        System.setProperty("org.geotools.referencing.forceXY", "true");
        PropertyUtil.addProperty("routing.forceXY", "true");
        mockIOHelper = mockStatic(IOHelper.class);
        mockHttpURLConnection = mock(HttpURLConnection.class);

    }

    @AfterEach
    public void goAway() throws Exception{
        System.clearProperty("org.geotools.referencing.forceXY");
        PropertyUtil.clearProperties();
        if (mockIOHelper != null) {
            mockIOHelper.close();
        }
    }
}

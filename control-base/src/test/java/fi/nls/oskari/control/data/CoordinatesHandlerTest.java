package fi.nls.oskari.control.data;

import static fi.nls.oskari.control.ActionConstants.*;

import fi.nls.oskari.control.ActionParamsException;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class CoordinatesHandlerTest extends JSONActionRouteTest {

    private CoordinatesHandler handler = new CoordinatesHandler();

    @Before
    public void setUp() throws Exception {
        handler.init();
    }

    private Map<String, String> getValidParams() {
        Map<String, String> params = new HashMap<>();
        //ain't it funny how the test will fail if we send these in wrong order, but when called from the actual code these need to be in reverse order or geotools will fail. funny.
        params.put(PARAM_LON, "23.7603118");
        params.put(PARAM_LAT, "61.4980214");
        params.put(PARAM_SRS, "EPSG:4326");
        params.put(handler.TARGET_SRS, "EPSG:3067");
        return params;
    }

    @Test(expected = ActionParamsException.class)
    public void testHandleActionNoParams()
            throws Exception {
        handler.handleAction(createActionParams());
    }

    @Test(expected = ActionParamsException.class)
    public void testHandleActionNoCoordinates()
            throws Exception {
        Map<String, String> params = getValidParams();
        params.remove(PARAM_LAT);
        handler.handleAction(createActionParams(params));
    }

    @Test(expected = ActionParamsException.class)
    public void testHandleActionNoSourceSRS()
            throws Exception {
        Map<String, String> params = getValidParams();
        params.remove(PARAM_SRS);
        handler.handleAction(createActionParams(params));
    }

    @Test(expected = ActionParamsException.class)
    public void testHandleActionNoTargetSRS()
            throws Exception {
        Map<String, String> params = getValidParams();
        params.remove(handler.TARGET_SRS);
        handler.handleAction(createActionParams(params));
    }

    @Test
    public void testHandleAction4326to3067()
            throws Exception {
        Map<String, String> params = getValidParams();
        handler.handleAction(createActionParams(params));

        assertEquals(PARAM_LAT, 6822546.781459001, getResponseJSON().getDouble(PARAM_LAT), 0.0);
        assertEquals(PARAM_LON, 327578.7810839222, getResponseJSON().getDouble(PARAM_LON), 0.0);
    }
    @Test
    public void testHandleAction3067to4326()
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_LON, "327578.7810839222");
        params.put(PARAM_LAT, "6822546.781459001");
        params.put(PARAM_SRS, "EPSG:3067");
        params.put(handler.TARGET_SRS, "EPSG:4326");
        handler.handleAction(createActionParams(params));

        assertEquals(PARAM_LAT, 61.4980214, getResponseJSON().getDouble(PARAM_LAT), 0.00001);
        assertEquals(PARAM_LON, 23.7603118, getResponseJSON().getDouble(PARAM_LON), 0.00001);

    }
}
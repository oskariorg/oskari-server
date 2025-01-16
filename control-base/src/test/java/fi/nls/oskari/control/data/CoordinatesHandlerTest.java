package fi.nls.oskari.control.data;

import fi.nls.oskari.control.ActionParamsException;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static fi.nls.oskari.control.ActionConstants.PARAM_LAT;
import static fi.nls.oskari.control.ActionConstants.PARAM_LON;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CoordinatesHandlerTest extends JSONActionRouteTest {

    private CoordinatesHandler handler = new CoordinatesHandler();

    @BeforeEach
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

    @Test()
    public void testHandleActionNoParams()
            throws Exception {
        assertThrows(ActionParamsException.class, () -> {
            handler.handleAction(createActionParams());
        });
    }

    @Test()
    public void testHandleActionNoCoordinates()
            throws Exception {
        assertThrows(ActionParamsException.class, () -> {
            Map<String, String> params = getValidParams();
            params.remove(PARAM_LAT);
            handler.handleAction(createActionParams(params));
        });
    }

    @Test()
    public void testHandleActionNoSourceSRS()
            throws Exception {
        assertThrows(ActionParamsException.class, () -> {
            Map<String, String> params = getValidParams();
            params.remove(PARAM_SRS);
            handler.handleAction(createActionParams(params));
        });
    }

    @Test()
    public void testHandleActionNoTargetSRS()
            throws Exception {
        assertThrows(ActionParamsException.class, () -> {
            Map<String, String> params = getValidParams();
            params.remove(handler.TARGET_SRS);
            handler.handleAction(createActionParams(params));
        });
    }

    @Test
    @Disabled
    // java.lang.NullPointerException: Cannot invoke "java.io.PrintWriter.print(Object)" because the return value of "javax.servlet.http.HttpServletResponse.getWriter()" is null
    public void testHandleAction4326to3067()
            throws Exception {
        // TODO: fix me and a bunch of others with the same problem:
        // fi.nls.oskari.control.ActionParamsException: Cannot invoke "java.io.PrintWriter.print(Object)" because the return value of "javax.servlet.http.HttpServletResponse.getWriter()" is null
        Map<String, String> params = getValidParams();
        handler.handleAction(createActionParams(params));

        assertEquals(6822546.781459001, getResponseJSON().getDouble(PARAM_LAT), 0.0, PARAM_LAT);
        assertEquals(327578.78108392254, getResponseJSON().getDouble(PARAM_LON), 1e-9, PARAM_LON);
    }
    @Test
    @Disabled
    // java.lang.NullPointerException: Cannot invoke "java.io.PrintWriter.print(Object)" because the return value of "javax.servlet.http.HttpServletResponse.getWriter()" is null
    public void testHandleAction3067to4326()
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_LON, "327578.7810839222");
        params.put(PARAM_LAT, "6822546.781459001");
        params.put(PARAM_SRS, "EPSG:3067");
        params.put(handler.TARGET_SRS, "EPSG:4326");
        handler.handleAction(createActionParams(params));

        assertEquals(61.4980214, getResponseJSON().getDouble(PARAM_LAT), 0.00001, PARAM_LAT);
        assertEquals(23.7603118, getResponseJSON().getDouble(PARAM_LON), 0.00001, PARAM_LON);

    }
}
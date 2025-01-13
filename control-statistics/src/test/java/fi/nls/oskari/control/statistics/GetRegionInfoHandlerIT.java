package fi.nls.oskari.control.statistics;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*"})
public class GetRegionInfoHandlerIT {

    @BeforeClass
    public static void init() throws IllegalArgumentException, SQLException {
        TestHelper.registerTestDataSource();
    }

    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test(timeout = 120000)
    public void testGettingRegionInfo() throws ActionException, JSONException {
        GetRegionsHandler handler = new GetRegionsHandler();
        handler.init();
        // Note: This test expects "oskari:kunnat2013" to be a layer id 9.
        JSONObject result = handler.getRegionInfoJSON(9, "EPSG:3067");
        assertEquals("Alajärvi", result.getJSONObject("005").getString("name").toString());
    }

    @Test(timeout = 120000)
    public void testGettingRegionInfoForErva() throws ActionException, JSONException {
        GetRegionsHandler handler = new GetRegionsHandler();
        handler.init();
        // Note: This test expects "oskari:erva-alueet" to be a layer id 11.
        JSONObject result = handler.getRegionInfoJSON(11, "EPSG:3067");
        assertEquals("Helsingin Yliopistosairaalan erityisvastuualue", result.getJSONObject("1").getString("name").toString());
    }
}


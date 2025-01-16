package fi.nls.oskari.control.statistics;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

@ExtendWith(MockitoExtension.class)
public class GetRegionInfoHandlerIT {

    @BeforeAll
    public static void init() throws IllegalArgumentException, SQLException {
        TestHelper.registerTestDataSource();
    }

    @AfterAll
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test
    @Disabled
    //[WARN] fi.nls.oskari.map.view.AppSetupServiceMybatisImpl: Exception while init deafult view id :
    //### Error querying database.  Cause: org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "oskari_appsetup" not found (this database is empty); SQL statement
    public void testGettingRegionInfo() throws ActionException, JSONException {
        assertTimeout(Duration.ofMillis(120000), () -> {
            GetRegionsHandler handler = new GetRegionsHandler();
            handler.init();
            // Note: This test expects "oskari:kunnat2013" to be a layer id 9.
            JSONObject result = handler.getRegionInfoJSON(9, "EPSG:3067");
            assertEquals("Alajärvi", result.getJSONObject("005").getString("name").toString());
        });
    }

    @Test()
    @Disabled
    //[WARN] fi.nls.oskari.map.view.AppSetupServiceMybatisImpl: Exception while init deafult view id :
    //### Error querying database.  Cause: org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "oskari_appsetup" not found (this database is empty); SQL statement
    public void testGettingRegionInfoForErva() throws ActionException, JSONException {
        assertTimeout(Duration.ofMillis(120000), () -> {
            GetRegionsHandler handler = new GetRegionsHandler();
            handler.init();
            // Note: This test expects "oskari:erva-alueet" to be a layer id 11.
            JSONObject result = handler.getRegionInfoJSON(11, "EPSG:3067");
            assertEquals("Helsingin Yliopistosairaalan erityisvastuualue", result.getJSONObject("1").getString("name").toString());
        });
    }
}


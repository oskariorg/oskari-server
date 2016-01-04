package fi.nls.oskari.control.statistics.plugins;

import org.apache.commons.dbcp2.BasicDataSource;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.util.PropertyUtil;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import javax.naming.NamingException;
import javax.sql.DataSource;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatasourceHelper.class)
@PowerMockIgnore( {"javax.management.*"}) 
public class GetIndicatorsMetadataHandlerIT {
    public static class DatasourceHelperMock extends DatasourceHelper {
        public DatasourceHelperMock() {
            super();
        }
        @Override
        public DataSource getDataSource(String name) {
            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName("org.postgresql.Driver");
            basicDataSource.setUrl(PropertyUtil.get("db.url"));
            basicDataSource.setUsername(PropertyUtil.get("db.username"));
            basicDataSource.setPassword(PropertyUtil.get("db.password"));
            return basicDataSource;
        }
    }
    
    @BeforeClass
    public static void init() throws NamingException, IllegalArgumentException,
        IllegalAccessException {
        PropertyUtil.loadProperties("/oskari-ext.properties");
        Field field = PowerMockito.field(DatasourceHelper.class, "INSTANCE");
        field.set(DatasourceHelper.class, new DatasourceHelperMock());
    }
    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }
    @Test(timeout=120000)
    public void testGettingIndicatorMetadata() throws ActionException, JSONException {
        GetIndicatorsMetadataHandler handler = new GetIndicatorsMetadataHandler();
        handler.init();
        JSONObject result = handler.getIndicatorsMetadataJSON(null, false);
        JSONObject sotkaIndicatorsInfo = result.getJSONObject(
                "fi.nls.oskari.control.statistics.plugins."
                + "sotka.SotkaStatisticalDatasourcePlugin");
        JSONObject sotkaIndicators = sotkaIndicatorsInfo.getJSONObject("indicators");
        JSONObject demographicsIndicator = sotkaIndicators.getJSONObject("169");
        assertEquals("25 - 64-vuotiaat, % vÃ¤estÃ¶stÃ¤", demographicsIndicator.getJSONObject("name").getString("fi"));

        GetIndicatorSelectorMetadataHandler metadataHandler = new GetIndicatorSelectorMetadataHandler();
        metadataHandler.init();
        JSONObject demographicsResult = metadataHandler.getIndicatorMetadataJSON(null,
                "fi.nls.oskari.control.statistics.plugins."
                        + "sotka.SotkaStatisticalDatasourcePlugin", "169");
        
        assertEquals(2, demographicsResult.getJSONArray("selectors").length());
        assertEquals("sex", demographicsResult.getJSONArray("selectors").getJSONObject(0).getString("id"));
        assertEquals(3, demographicsResult.getJSONArray("selectors").getJSONObject(0)
                .getJSONArray("allowedValues").length());
        assertEquals("year", demographicsResult.getJSONArray("selectors").getJSONObject(1).getString("id"));
        assertEquals(25, demographicsResult.getJSONArray("selectors").getJSONObject(1)
                .getJSONArray("allowedValues").length());
    }
}


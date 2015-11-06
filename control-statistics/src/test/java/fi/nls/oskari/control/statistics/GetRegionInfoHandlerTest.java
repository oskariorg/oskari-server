package fi.nls.oskari.control.statistics;

import org.apache.commons.dbcp2.BasicDataSource;
import org.json.JSONArray;
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
public class GetRegionInfoHandlerTest {
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
    public void testGettingRegionInfo() throws ActionException, JSONException {
        GetRegionInfoHandler handler = new GetRegionInfoHandler();
        handler.init();
        JSONObject result = handler.getRegionInfoJSON("oskari:kunnat2013", "005");
        assertEquals("Alajärvi",
                result.getJSONObject("005").getString("name").toString());
    }
}


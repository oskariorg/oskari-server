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
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.util.PropertyUtil;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import javax.naming.NamingException;
import javax.sql.DataSource;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatasourceHelper.class)
@PowerMockIgnore( {"javax.management.*"}) 
public class GetIndicatorMetadataHandlerTest {
    public static class DatasourceHelperMock extends DatasourceHelper {
        public DatasourceHelperMock() {
            super();
        }
        @Override
        public DataSource getDataSource(String name) {
            BasicDataSource basicDataSource = new BasicDataSource();
            // FIXME: Get these from properties.
            basicDataSource.setDriverClassName("org.postgresql.Driver");
            basicDataSource.setUrl("jdbc:postgresql://localhost:5432/oskaridb");
            basicDataSource.setUsername("admin");
            basicDataSource.setPassword("admin");
            return basicDataSource;
        }
    }
    
    @BeforeClass
    public static void init() throws NamingException, IllegalArgumentException,
        IllegalAccessException {
        PropertyUtil.loadProperties("/oskari-ext.properties");
        Field field = PowerMockito.field(DatasourceHelper.class, "INSTANCE");
        field.set(DatasourceHelper.class, new DatasourceHelperMock());
        
        SotkaStatisticalDatasourcePlugin.testMode = true;
    }
    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }
    @Test(timeout=120000)
    public void testGettingIndicatorMetadata() throws ActionException, JSONException {
        GetIndicatorMetadataHandler handler = new GetIndicatorMetadataHandler();
        handler.init();
        JSONObject result = handler.getIndicatorMetadataJSON();
        JSONObject sotkaIndicatorsInfo = result.getJSONObject("fi.nls.oskari.control.statistics.plugins."
                + "sotka.SotkaStatisticalDatasourcePlugin");
        JSONObject sotkaIndicators = sotkaIndicatorsInfo.getJSONObject("indicators");
        JSONObject demographicsIndicator = sotkaIndicators.getJSONObject("169");
        
        assertEquals("[{\"id\":\"sex\",\"allowedValues\":[\"male\",\"female\","
                + "\"total\"]},{\"id\":\"year\",\"allowedValues\":"
                + "[\"1990\",\"1991\",\"1992\",\"1993\",\"1994\",\"1995\","
                + "\"1996\",\"1997\",\"1998\",\"1999\",\"2000\",\"2001\","
                + "\"2002\",\"2003\",\"2004\",\"2005\",\"2006\",\"2007\","
                + "\"2008\",\"2009\",\"2010\",\"2011\",\"2012\",\"2013\","
                + "\"2014\"]}]",
                demographicsIndicator.getJSONArray("selectors").toString());
    }
}


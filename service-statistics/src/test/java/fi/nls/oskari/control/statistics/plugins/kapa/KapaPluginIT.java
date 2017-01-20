package fi.nls.oskari.control.statistics.plugins.kapa;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.*;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.ResourceHelper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.Mockito.*;

/**
 * A test for the plugin manager interaction with the KaPa plugin.
 * @author tero
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DatasourceHelper.class, IOHelper.class})
@PowerMockIgnore( {"javax.management.*"}) 
public class KapaPluginIT {

    final private StatisticalDatasourcePluginManager manager = StatisticalDatasourcePluginManager.getInstance();
    private static String testIndicatorsResponse = ResourceHelper.readStringResource("KapaIndicators.json",
            KapaPluginIT.class);
    private static String testIndicatorDataResponse = ResourceHelper.readStringResource("KapaIndicatorData.json",
            KapaPluginIT.class);
    private static String url = "";

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
    public static void init() throws IOException, IllegalArgumentException, IllegalAccessException {
        PropertyUtil.loadProperties("/oskari-ext.properties");
        PowerMockito.mockStatic(IOHelper.class);
        when(IOHelper.getConnection(any(String.class))).then(new Answer<HttpURLConnection>() {
            @Override
            public HttpURLConnection answer(InvocationOnMock invocation) throws Throwable {
                url = invocation.getArguments()[0].toString();
                return new HttpURLConnection(null) {
                    @SuppressWarnings("deprecation")
                    @Override
                    public InputStream getInputStream() {
                        return new StringBufferInputStream(testIndicatorsResponse);
                    }
                    @Override
                    public void disconnect() {
                    }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() throws IOException {
                    }
                };
            }
        });
        when(IOHelper.readString(any(InputStream.class), any(String.class))).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if (url.startsWith("file:///opt/oskari/mockresponses/1.0/indicators/")) {
                    // Querying single indicator.
                    return testIndicatorDataResponse;
                } else {
                    // Querying all indicators.
                    return testIndicatorsResponse;
                }
            }
            
        });
        Field field = PowerMockito.field(DatasourceHelper.class, "INSTANCE");
        field.set(DatasourceHelper.class, new DatasourceHelperMock());
    }
    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test(timeout=120000)
    public void testKapaPlugin()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        manager.reset();
        StatisticalDatasource source = new StatisticalDatasource();
        source.setId(1);
        KapaStatisticalDatasourceFactory factory = new KapaStatisticalDatasourceFactory();
        source.setPlugin(factory.getName());
        manager.registerDatasource(source, factory);
        assertEquals(1, manager.getPlugins().size());
        StatisticalDatasourcePlugin kapaPlugin = null;
        Iterator<StatisticalDatasourcePlugin> pluginsIterator = manager.getPlugins().values().iterator();
        while (kapaPlugin == null && pluginsIterator.hasNext()) {
            StatisticalDatasourcePlugin nextPlugin = pluginsIterator.next();
            if (nextPlugin instanceof KapaStatisticalDatasourcePlugin) {
                kapaPlugin = nextPlugin;
            }
        }
        assertNotNull("KaPa plugin was not found.", kapaPlugin);
        
        // Getting indicators.
        IndicatorSet indicatorSet = kapaPlugin.getIndicatorSet(null);
        List<StatisticalIndicator> indicators = indicatorSet.getIndicators();
        assertTrue("Indicators result was too small.", indicators.size() > 1);
        
        StatisticalIndicatorDataModel selectors = indicators.get(0).getDataModel();
        List<StatisticalIndicatorDataDimension> allSelectors = selectors.getDimensions();
        for (StatisticalIndicatorDataDimension selector : allSelectors) {
            // Selecting the first allowed value for each selector to define a proper selector.
            selector.setValue(selector.getAllowedValues().iterator().next().getKey());
        }
        Map<String, IndicatorValue> indicatorValues = kapaPlugin.getIndicatorValues(indicators.get(0), selectors, indicators.get(0).getLayers().get(0));
        assertNotNull("Indicator values response was null.", indicatorValues);
        assertTrue("IndicatorValues result was too small: " + String.valueOf(indicatorValues), indicatorValues.size() > 2);
    }
}


package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.util.PropertyUtil;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatasourceHelper.class)
@PowerMockIgnore( {"javax.management.*"}) 
public class StatisticalDatasourcePluginManagerIT {

    final private StatisticalDatasourcePluginManager manager = StatisticalDatasourcePluginManager.getInstance();

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
    public static void init() throws IllegalArgumentException, IllegalAccessException {
        PropertyUtil.loadProperties("/oskari-ext.properties");
        Field field = PowerMockito.field(DatasourceHelper.class, "INSTANCE");
        field.set(DatasourceHelper.class, new DatasourceHelperMock());
    }
    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }
    @Test
    public void testRegisteringPluginsAndRetrievingThem()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        manager.reset();
        StatisticalDatasource source = new StatisticalDatasource();
        source.setId(1);
        MockPluginFactory factory = new MockPluginFactory();
        source.setPlugin(factory.getName());
        manager.registerDatasource(source, factory);
        assertEquals(1, manager.getPlugins().size());
        assertThat(manager.getPlugins().values().iterator().next(),
                instanceOf(MockPlugin.class));
    }

    @Test(timeout=120000)
    public void testSotkaPlugin()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        manager.reset();
        StatisticalDatasource source = new StatisticalDatasource();
        source.setId(1);
        source.setPlugin("SotkaNET");
        manager.registerDatasource(source, new MockPluginFactory());
        assertEquals(1, manager.getPlugins().size());
        StatisticalDatasourcePlugin sotkaPlugin = null;
        Iterator<StatisticalDatasourcePlugin> pluginsIterator = manager.getPlugins().values().iterator();
        while (sotkaPlugin == null && pluginsIterator.hasNext()) {
            StatisticalDatasourcePlugin nextPlugin = pluginsIterator.next();
            if (nextPlugin instanceof SotkaStatisticalDatasourcePlugin) {
                sotkaPlugin = nextPlugin;
            }
        }
        assertNotNull("SotkaNET plugin was not found.", sotkaPlugin);
        
        // Getting indicators.
        IndicatorSet indicatorSet = sotkaPlugin.getIndicatorSet(null);
        List<StatisticalIndicator> indicators = indicatorSet.getIndicators();
        assertTrue("Indicators result was too small.", indicators.size() > 10);
        
        StatisticalIndicatorDataModel selectors = indicators.get(0).getDataModel();
        List<StatisticalIndicatorDataDimension> allSelectors = selectors.getDimensions();
        for (StatisticalIndicatorDataDimension selector : allSelectors) {
            // Selecting the first allowed value for each selector to define a proper selector.
            selector.setValue(selector.getAllowedValues().iterator().next().getKey());
        }
        Map<String, IndicatorValue> indicatorValues = sotkaPlugin.getIndicatorValues(indicators.get(0), selectors, indicators.get(0).getLayers().get(0));
        assertNotNull("Indicator values response was null.", indicatorValues);
        assertTrue("IndicatorValues result was too small: " + String.valueOf(indicatorValues), indicatorValues.size() > 2);
    }
}


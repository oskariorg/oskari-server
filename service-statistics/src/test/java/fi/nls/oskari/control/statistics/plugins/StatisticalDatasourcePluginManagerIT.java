package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.data.IndicatorSet;
import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class StatisticalDatasourcePluginManagerIT {

    final private StatisticalDatasourcePluginManager manager = StatisticalDatasourcePluginManager.getInstance();
    
    @BeforeAll
    public static void init() throws IllegalArgumentException, IllegalAccessException, SQLException {
        TestHelper.registerTestDataSource();
    }
    @AfterAll
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

        Boolean isMockPlugin = manager.getPlugins().values().iterator().next() instanceof MockPlugin;
        assertTrue(isMockPlugin);
    }

    @Test()
    public void testSotkaPlugin()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        assertTimeout(Duration.ofMillis(120000), () -> {
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
            assertNotNull(sotkaPlugin, "SotkaNET plugin was not found.");

            // Getting indicators.
            IndicatorSet indicatorSet = sotkaPlugin.getIndicatorSet(null);
            List<StatisticalIndicator> indicators = indicatorSet.getIndicators();
            assertTrue(indicators.size() > 10, "Indicators result was too small.");

            StatisticalIndicatorDataModel selectors = indicators.get(0).getDataModel();
            List<StatisticalIndicatorDataDimension> allSelectors = selectors.getDimensions();
            for (StatisticalIndicatorDataDimension selector : allSelectors) {
                // Selecting the first allowed value for each selector to define a proper selector.
                selector.setValue(selector.getAllowedValues().iterator().next().getKey());
            }
            Map<String, IndicatorValue> indicatorValues = sotkaPlugin.getIndicatorValues(indicators.get(0), selectors, indicators.get(0).getLayers().get(0));
            assertNotNull(indicatorValues, "Indicator values response was null.");
            assertTrue(indicatorValues.size() > 2, "IndicatorValues result was too small: " + String.valueOf(indicatorValues));

        });
    }
}


package fi.nls.oskari.control.statistics.plugins;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin;
import fi.nls.oskari.util.PropertyUtil;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

@RunWith(PowerMockRunner.class)
public class StatisticalDatasourcePluginManagerTest {

    final private StatisticalDatasourcePluginManager manager = new StatisticalDatasourcePluginManager();

    @BeforeClass
    public static void init() {
        PropertyUtil.loadProperties("/oskari-ext.properties");
    }
    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }
    @Test
    public void testRegisteringPluginsAndRetrievingThem()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        manager.registerPlugin("fi.nls.oskari.control.statistics.plugins.MockPlugin");
        assertEquals(1, manager.getPlugins().size());
        assertThat(manager.getPlugins().iterator().next(),
                instanceOf(MockPlugin.class));
    }

    @Test
    public void testSotkaPlugin()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        manager.registerPlugin("fi.nls.oskari.control.statistics.plugins.sotka.SotkaStatisticalDatasourcePlugin");
        assertEquals(2, manager.getPlugins().size());
        StatisticalDatasourcePlugin sotkaPlugin = null;
        Iterator<StatisticalDatasourcePlugin> pluginsIterator = manager.getPlugins().iterator();
        while (sotkaPlugin == null && pluginsIterator.hasNext()) {
            StatisticalDatasourcePlugin nextPlugin = pluginsIterator.next();
            if (nextPlugin instanceof SotkaStatisticalDatasourcePlugin) {
                sotkaPlugin = nextPlugin;
            }
        }
        assertNotNull("SotkaNET plugin was not found.", sotkaPlugin);
        
        // Getting indicators.
        List<? extends StatisticalIndicator> indicators = sotkaPlugin.getIndicators();
        assertTrue("Indicators result was too small.", indicators.size() > 10);
    }
}


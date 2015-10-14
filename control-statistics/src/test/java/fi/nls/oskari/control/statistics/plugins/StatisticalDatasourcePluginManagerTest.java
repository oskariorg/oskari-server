package fi.nls.oskari.control.statistics.plugins;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class StatisticalDatasourcePluginManagerTest {

    final private StatisticalDatasourcePluginManager manager = new StatisticalDatasourcePluginManager();

    @Test
    public void testRegisteringPluginsAndRetrievingThem()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // FIXME: Test this completely.
        // manager.registerPlugin("fi.nls.oskari.control.statistics.plugins.MockPlugin");
    }
}


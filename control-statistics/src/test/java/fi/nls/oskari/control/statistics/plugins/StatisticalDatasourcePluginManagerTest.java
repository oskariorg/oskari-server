package fi.nls.oskari.control.statistics.plugins;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(PowerMockRunner.class)
public class StatisticalDatasourcePluginManagerTest {

    final private StatisticalDatasourcePluginManager manager = new StatisticalDatasourcePluginManager();

    @Test
    public void testRegisteringPluginsAndRetrievingThem()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        manager.registerPlugin("fi.nls.oskari.control.statistics.plugins.MockPlugin");
        assertEquals(1, manager.getPlugins().size());
        assertThat(manager.getPlugins().iterator().next(),
                instanceOf(MockPlugin.class));
    }
}


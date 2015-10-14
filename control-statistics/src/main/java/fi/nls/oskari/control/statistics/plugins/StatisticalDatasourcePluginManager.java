package fi.nls.oskari.control.statistics.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * This manager keeps a list of registered data source plugins.
 */
public class StatisticalDatasourcePluginManager {
    private static final Logger LOG = LogFactory.getLogger(StatisticalDatasourcePluginManager.class);

    /**
     * We are using a static plugin store, because we don't have a IoC container (e.g. Spring) here yet.
     * This is in any case a singleton, this is making it explicit.
     */
    private static final Map<String, StatisticalDatasourcePlugin> plugins =
            new HashMap<String, StatisticalDatasourcePlugin>();

    /**
     * Use this method to register plugins as data sources.
     * @param className The fully qualified name of the plugin class. Must be in the classpath.
     * @throws ClassNotFoundException If the class is not found in the classpath.
     * @throws IllegalAccessException If the class no-parameter constructor is not accessible.
     * @throws InstantiationException If there was an exception in instantiating the plugin.
     */
    public void registerPlugin(String className) throws
    ClassNotFoundException, InstantiationException, IllegalAccessException {
        
        Class<? extends StatisticalDatasourcePlugin> pluginClass =
                Class.forName(className).asSubclass(StatisticalDatasourcePlugin.class);
        StatisticalDatasourcePlugin plugin = pluginClass.newInstance();
        LOG.info("Registering a Statistical Datasource: " + className);
        plugins.put(className, plugin);
    }
    
    /**
     * 
     * @return The collection of registered plugins.
     */
    public Collection<StatisticalDatasourcePlugin> getPlugins() {
        return plugins.values();
    }
}

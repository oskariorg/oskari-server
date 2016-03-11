package fi.nls.oskari.control.statistics.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasourceMapper;
import fi.nls.oskari.db.DatasourceHelper;
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
    private static final Map<String, StatisticalDatasourcePlugin> plugins = new HashMap<>();
    /**
     * Localizations with "name" keys for plugin data sources to show to users in the frontend.
     */
    private static final Map<String, String> pluginLocales = new HashMap<>();

    private List<StatisticalDatasource> pluginInfos;

    /**
     * Use this method to register plugins as data sources.
     * @param className The fully qualified name of the plugin class. Must be in the classpath.
     * @param locale The JSON locale with "name" key for the localized text to show in the UI to resolve to the data source name for the plugin.
     * @throws ClassNotFoundException If the class is not found in the classpath.
     * @throws IllegalAccessException If the class no-parameter constructor is not accessible.
     * @throws InstantiationException If there was an exception in instantiating the plugin.
     */
    public void registerPlugin(String className, String locale) throws
    ClassNotFoundException, InstantiationException, IllegalAccessException {
        
        Class<? extends StatisticalDatasourcePlugin> pluginClass =
                Class.forName(className).asSubclass(StatisticalDatasourcePlugin.class);
        StatisticalDatasourcePlugin plugin = pluginClass.newInstance();
        LOG.info("Registering a Statistical Datasource: " + className);
        plugin.init();
        plugins.put(className, plugin);
        if (locale == null || locale.equals("")) {
            // If the localization key was not defined, we will use the class name.
            pluginLocales.put(className, className);
        } else {
            pluginLocales.put(className, locale);
        }
    }
    
    /**
     * 
     * @return The collection of registered plugins.
     */
    public Collection<StatisticalDatasourcePlugin> getPlugins() {
        return plugins.values();
    }
    /**
     * 
     * @return One plugin.
     */
    public StatisticalDatasourcePlugin getPlugin(String id) {
        return plugins.get(id);
    }
    /**
     * Used for tests.
     */
    public void reset() {
        plugins.clear();
    }

    public void init() {
        // Fetching the plugins from the database.
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        SqlSessionFactory factory = initializeIBatis(dataSource);
        final SqlSession session = factory.openSession();
        pluginInfos = session.selectList("getAll");
        System.out.println("Plugin infos: " + String.valueOf(pluginInfos));
        for (StatisticalDatasource pluginInfo: pluginInfos) {
            LOG.info("Adding plugin from database: " + String.valueOf(pluginInfo));
            try {
                this.registerPlugin(pluginInfo.getClassName(), pluginInfo.getLocale());
            } catch (ClassNotFoundException e) {
                LOG.error("Could not find the plugin class: " + pluginInfo.getClassName() + ". Skipping...");
            } catch (InstantiationException e) {
                LOG.error("Could not instantiate the plugin class: " + pluginInfo.getClassName() + ". Skipping...");
            } catch (IllegalAccessException e) {
                LOG.error("Could not access the plugin class constructor: " + pluginInfo.getClassName() + ". Skipping...");
            }
        }
    }
    
    private SqlSessionFactory initializeIBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(StatisticalDatasource.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(StatisticalDatasourceMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public String getPluginLocale(Class<? extends StatisticalDatasourcePlugin> pluginClass) {
        return pluginLocales.get(pluginClass.getCanonicalName());
    }
}

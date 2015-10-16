package fi.nls.oskari.control.statistics.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

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
    private static final Map<String, StatisticalDatasourcePlugin> plugins =
            new HashMap<String, StatisticalDatasourcePlugin>();

    private SqlSessionFactory factory;

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
        plugin.init();
        plugins.put(className, plugin);
    }
    
    /**
     * 
     * @return The collection of registered plugins.
     */
    public Collection<StatisticalDatasourcePlugin> getPlugins() {
        return plugins.values();
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
        factory = initializeIBatis(dataSource);
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

    public void getLocalizedPluginName(Class<? extends StatisticalDatasourcePlugin> class1) {
        
    }
}

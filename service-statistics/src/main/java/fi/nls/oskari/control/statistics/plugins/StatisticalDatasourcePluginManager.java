package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasourceMapper;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.JSONLocalized;
import fi.nls.oskari.domain.map.JSONLocalizedName;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This manager keeps a list of registered data source plugins.
 */
public class StatisticalDatasourcePluginManager {
    private static final Logger LOG = LogFactory.getLogger(StatisticalDatasourcePluginManager.class);

    private static StatisticalDatasourcePluginManager instance = null;
    /**
     * We are using a static plugin store, because we don't have a IoC container (e.g. Spring) here yet.
     * This is in any case a singleton, this is making it explicit.
     */
    private static final Map<Long, StatisticalDatasourcePlugin> plugins = new HashMap<>();

    // The collection of registered plugins.
    public Map<Long, StatisticalDatasourcePlugin> getPlugins() {
        return plugins;
    }

    public StatisticalDatasourcePlugin getPlugin(long id) {
        return plugins.get(id);
    }

    public static StatisticalDatasourcePluginManager getInstance() {
        if(instance == null) {
            instance = new StatisticalDatasourcePluginManager();
            instance.init();
        }
        return instance;
    }

    private StatisticalDatasourcePluginManager() {}

    /**
     * Returns a list of successfully registered datasources.
     * There might be more in the database, but misconfigured ones are not registered.
     * @return
     */
    public List<StatisticalDatasource> getDatasources() {
        List<StatisticalDatasource> list = new ArrayList<>();
        for(StatisticalDatasourcePlugin plugin : plugins.values()) {
            list.add(plugin.getSource());
        }
        return list;
    }

    /**
     * Used for tests.
     */
    public void reset() {
        plugins.clear();
    }

    public void init() {
        List<StatisticalDatasource> statisticalDatasources = getConfiguredDatasources();
        Map<String, StatisticalDatasourceFactory> allPlugins = OskariComponentManager.getComponentsOfType(StatisticalDatasourceFactory.class);

        for (StatisticalDatasource source : statisticalDatasources) {
            LOG.info("Adding plugin from database: ", source);
            try {
                StatisticalDatasourceFactory plugin = allPlugins.get(source.getPlugin());
                if (plugin == null) {
                    throw new ClassNotFoundException("Annotation @Oskari(\"" + source.getPlugin() + "\") not found!");
                }
                this.registerDatasource(source, plugin);
            } catch (ClassNotFoundException e) {
                LOG.error("Could not find the plugin class: " + source.getPlugin() + ". Skipping...");
            }
        }
    }

    /**
     * Use this method to register plugins as data sources.
     *
     * @param source information about the datasource
     * @param factory factory to create plugins for the source
     */
    public void registerDatasource(StatisticalDatasource source, StatisticalDatasourceFactory factory) {
        LOG.info("Registering a Statistical Datasource:", source.getId(), "with plugin", source.getPlugin());
        try {
            factory.setupSourceLayers(source);
            StatisticalDatasourcePlugin plugin = factory.create(source);
            plugins.put(source.getId(), plugin);
        } catch (APIException ex) {
            LOG.error("Couldn't register a Statistical Datasource:", source.getId(),
                    "with plugin", source.getPlugin(), "Reason:", ex.getMessage());
        }
    }

    private List<StatisticalDatasource> getConfiguredDatasources() {
        // Fetching the plugins from the database.
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        final SqlSessionFactory factory = initializeIBatis(dataSource);
        final SqlSession session = factory.openSession();
        try {
            return session.getMapper(StatisticalDatasourceMapper.class).getAll();
        }
        finally {
            session.close();
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
}

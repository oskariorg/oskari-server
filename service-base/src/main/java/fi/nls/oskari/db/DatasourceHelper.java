package fi.nls.oskari.db;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMAKINEN on 11.6.2015.
 */
public class DatasourceHelper {

    private static final Logger LOGGER = LogFactory.getLogger(DatasourceHelper.class);
    private static final String DEFAULT_DATASOURCE_NAME = "jdbc/OskariPool";
    private static final String MSG_CHECKING_POOL = "Checking existance of database pool: %s";
    private static final String PREFIX_DB = "db.";
    private static final DatasourceHelper INSTANCE = new DatasourceHelper();
    private static final String KEY_MODULE_LIST = "db.additional.modules";

    private List<BasicDataSource> localDataSources = new ArrayList<>();
    private final static String JNDI_PREFIX = "java:comp/env/";
    private Context context;

    protected DatasourceHelper() {
        // use getInstance()
    }

    public static DatasourceHelper getInstance() {
        return INSTANCE;
    }

    public static String[] getAdditionalModules() {
        return PropertyUtil.getCommaSeparatedList(KEY_MODULE_LIST);
    }

    public static boolean isModuleEnabled(String name) {
        for(String key: getAdditionalModules()) {
            if(key.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public String getOskariDataSourceName() {
        return getOskariDataSourceName(null);
    }



    /**
     * Returns the configured datasource name matching module (db[.module].jndi.name)
     * from properties (or defaults to jdbc/OskariPool if not configured)
     * @param prefix module name like myplaces, analysis etc null means "core" oskari
     * @return defaults to jdbc/OskariPool if not configured
     */
    public String getOskariDataSourceName(final String prefix) {
        final String poolToken = (prefix == null) ? "" : prefix + ".";
        return PropertyUtil.get(PREFIX_DB + poolToken + "jndi.name", DEFAULT_DATASOURCE_NAME);
    }

    /**
     * Returns the default datasource.
     */
    public DataSource getDataSource() {
        return getDataSource(getContext(), getOskariDataSourceName());
    }
    /**
     * Returns a datasource matching the name
     * @param name for example jdbc/OskariPool
     */
    public DataSource getDataSource(final String name) {
        return getDataSource(getContext(), name);
    }
    /**
     * Returns a datasource matching the name from locally created ones or from given context if not created by this code.
     * @param name for example jdbc/OskariPool
     */
    public DataSource getDataSource(final Context ctx, final String name) {
        String dsName;
        if (name == null) {
            dsName = DEFAULT_DATASOURCE_NAME;
        } else {
            dsName = name;
        }
        DataSource dataSrc = localDataSources.stream()
                .filter(ds -> dsName.equals(ds.getJmxName()))
                .findFirst()
                .orElse(null);
        if (dataSrc != null) {
            return dataSrc;
        }
        try {
            LOGGER.info("Trying JNDI dataSource with name: " + dsName);
            return (DataSource) ctx.lookup(JNDI_PREFIX + dsName);
        } catch (Exception ex) {
            LOGGER.info("Couldn't find pool with name '" + dsName + "': " + ex.getMessage());
        }
        return null;
    }


    public boolean checkDataSource(final Context ctx) {
        return checkDataSource(ctx, null);
    }

    /**
     * Check that we have a functioning datasource matching the module (prefix).
     * Creates the datasource using properties if it's not available on the context.
     * @param ctx
     * @param prefix module like myplaces, analysis
     * @return
     */
    public boolean checkDataSource(final Context ctx, final String prefix) {
        final String poolName = getOskariDataSourceName(prefix);
        LOGGER.info(String.format(MSG_CHECKING_POOL, poolName));
        final DataSource ds = getDataSource(ctx, poolName);
        if (ds != null) {
            // using container provided datasource rather than one created by us
            LOGGER.debug("Found dataSource for name: " + poolName);
            return true;
        }
        LOGGER.info("Creating a DB DataSource based on configured properties");
        return createDataSource(prefix) != null;
    }

    public BasicDataSource createDataSource() {
        return createDataSource(null);
    }

    /**
     * Creates the datasource for the module (prefix)
     * @param prefix for example myplaces, analysis
     * @return
     */
    public BasicDataSource createDataSource(final String prefix) {
        // check if we have the named connection already
        BasicDataSource ds = (BasicDataSource) getDataSource(null, getOskariDataSourceName(prefix));
        if (ds != null) {
            return ds;
        }

        final BasicDataSource dataSource = new BasicDataSource();
        ConnectionInfo info = getPropsForDS(prefix);

        dataSource.setDriverClassName(info.driver);
        dataSource.setUrl(info.url);
        dataSource.setUsername(info.user);
        dataSource.setPassword(info.pass);
        dataSource.setTimeBetweenEvictionRunsMillis(-1);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setValidationQueryTimeout(100);
        // Just for querying from local datasources when context can't be created (for example in Tomcat by default)
        dataSource.setJmxName(getOskariDataSourceName(prefix));
        try {
            // try getting connection. If it fails we can tell the admin that the config is not good and try JNDI instead
            dataSource.getConnection();
        } catch (SQLException e) {
            LOGGER.error(e, "Couldn't create database connection using:", info.url);
            // return null so we don't add a non-functioning datasource to localDataSources
            // AND this makes the code always try to get a connection using JNDI instead
            return null;
        }
        localDataSources.add(dataSource);
        return dataSource;
    }

    /**
     * Tries prefixed properties and defaults to non-prefixed versions
     * @param prefix
     * @return
     */
    public ConnectionInfo getPropsForDS(final String prefix) {
        ConnectionInfo info = new ConnectionInfo();
        final String poolToken = (prefix == null) ? "" : prefix + ".";
        info.driver = PropertyUtil.get("db.jndi.driverClassName", "org.postgresql.Driver");
        info.url = PropertyUtil.get(PREFIX_DB + poolToken + "url",
                PropertyUtil.get(PREFIX_DB + "url", "jdbc:postgresql://localhost:5432/oskaridb"));
        info.user = PropertyUtil.get(PREFIX_DB + poolToken + "username",
                PropertyUtil.get(PREFIX_DB + "username", ""));
        info.pass = PropertyUtil.get(PREFIX_DB + poolToken + "password",
                PropertyUtil.get(PREFIX_DB + "password", ""));
        return info;
    }

    /**
     * Creates an InitialContext if not created yet.
     */
    public Context getContext() {
        if (context == null) {
            try {
                context = new InitialContext();
            } catch (Exception ex) {
                LOGGER.error("Couldn't get context: ", ex.getMessage());
            }
        }
        return context;
    }

    /**
     * Clean up created resources
     */
    public void teardown() {
        // clean up created datasources
        for (BasicDataSource ds : localDataSources) {
            try {
                ds.close();
                LOGGER.debug("Closed locally created data source");
            } catch (final SQLException e) {
                LOGGER.error(e, "Failed to close locally created data source");
            }
        }
    }

}

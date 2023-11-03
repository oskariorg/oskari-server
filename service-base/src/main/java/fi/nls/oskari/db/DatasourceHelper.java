package fi.nls.oskari.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatasourceHelper {

    public static final String DEFAULT_DATASOURCE_NAME = "jdbc/OskariPool";
    private static final String MSG_CHECKING_POOL = "Checking existance of database pool: %s";
    private static final String PREFIX_DB = "db.";
    private static final DatasourceHelper INSTANCE = new DatasourceHelper();
    private static final String KEY_MODULE_LIST = "db.additional.modules";

    private Map<String, DataSource> localDataSources = new HashMap<>();
    public final static String JNDI_PREFIX = "java:comp/env/";
    private Context context;

    protected DatasourceHelper() {
        // use getInstance()
    }

    public static DatasourceHelper getInstance() {
        return INSTANCE;
    }

    private Logger getLogger() {
        return LogFactory.getLogger(DatasourceHelper.class);
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
        String poolName = name;
        if (name == null) {
            poolName = DEFAULT_DATASOURCE_NAME;
        }
        DataSource dataSrc = localDataSources.get(poolName);
        if (dataSrc != null) {
            return dataSrc;
        }
        try {
            return (DataSource) ctx.lookup(JNDI_PREFIX + poolName);
        } catch (Exception ex) {
            getLogger().error("Couldn't find pool with name '" + poolName + "': " + ex.getMessage());
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
        getLogger().info(String.format(MSG_CHECKING_POOL, poolName));
        final DataSource ds = getDataSource(ctx, poolName);
        if (ds != null) {
            // using container provided datasource rather than one created by us
            getLogger().debug("Found dataSource for name: " + poolName);
            return true;
        }
        getLogger().info("Creating a DB DataSource based on configured properties");
        return createDataSource(prefix) != null;
    }

    public DataSource createDataSource() {
        return createDataSource(null);
    }

    /**
     * Creates the datasource for the module (prefix)
     * @param prefix for example myplaces, analysis
     * @return
     */
    public DataSource createDataSource(final String prefix) {
        // check if we have the named connection already
        String poolName = getOskariDataSourceName(prefix);
        DataSource ds = getDataSource(null, poolName);
        if (ds != null) {
            return ds;
        }

        ConnectionInfo info = getPropsForDS(prefix);

        HikariConfig config = new HikariConfig();
        config.setPoolName(poolName);
        config.setDriverClassName(info.driver);
        config.setJdbcUrl(info.url);
        config.setUsername(info.user);
        config.setPassword(info.pass);
        config.setMaximumPoolSize(10);
        // config.addDataSourceProperty( "ApplicationName" , "should we provide one here or let users set this as part of the url?" );

        // Setting this statement cache is crucial for queries using postgis geometries like on userlayers with
        // WHERE user_layer_id = #{layerId} AND  geometry && ST_MAKEENVELOPE(#{minX}, #{minY}, #{maxX}, #{maxY}, #{srid})
        // Otherwise calling the query repeatedly the performance crashes from 50ms to around 4 seconds or so
        // https://stackoverflow.com/questions/64465108/spring-boot-jdbctemplate-disable-statement-cache
        config.addDataSourceProperty( "preparedStatementCacheQueries" , "0" );

        HikariDataSource dataSource = new HikariDataSource(config);
        try {
            registerDataSource(poolName, dataSource);
        } catch (SQLException e) {
            getLogger().error(e, "Couldn't create database connection using:", info.url);
            // return null so we don't add a non-functioning datasource to localDataSources
            // AND this makes the code always try to get a connection using JNDI instead
            return null;
        }
        return dataSource;
    }

    /**
     * Tests a connection from datasource and registers the datasource to local registry if its functional
     * @param poolName name to use like "jdbc/OskariPool"
     * @param dataSource the datasource to test and register
     * @throws SQLException if connection can't be opened/ds is broken
     */
    public void registerDataSource(String poolName, DataSource dataSource) throws SQLException {
        // Try getting connection:
        // If it fails we can tell the admin that the config is not good and try JNDI instead
        dataSource.getConnection().close();
        // if it works, register it
        localDataSources.put(poolName, dataSource);
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
                getLogger().error("Couldn't get context: ", ex.getMessage());
            }
        }
        return context;
    }

    /**
     * Clean up created resources
     */
    public void teardown() {
        // clean up created datasources
        for (DataSource ds : localDataSources.values()) {
            try {
                // try to close it
                if (ds instanceof BasicDataSource) {
                    ((BasicDataSource)ds).close();
                } else if (ds instanceof Closeable) {
                    ((AutoCloseable) ds).close();
                } else if (ds instanceof AutoCloseable) {
                    ((AutoCloseable) ds).close();
                }
                getLogger().debug("Closed locally created data source");
            } catch (final Exception e) {
                getLogger().error(e, "Failed to close locally created data source");
            }
        }
        localDataSources.clear();
    }

}

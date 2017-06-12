package fi.nls.oskari.db;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMAKINEN on 11.6.2015.
 */
public class DatasourceHelper {

    private static final Logger LOGGER = LogFactory.getLogger(DatasourceHelper.class);
    private static final String DEFAULT_DATASOURCE_NAME = "jdbc/OskariPool";
    private static final String MSG_CHECKING_POOL = " - checking existance of database pool: %s";
    private static final String PREFIX_DB = "db.";
    private static final DatasourceHelper INSTANCE = new DatasourceHelper();
    private static final String KEY_MODULE_LIST = "db.additional.modules";

    private List<BasicDataSource> localDataSources = new ArrayList<BasicDataSource>();
    private final static String JNDI_PREFIX = "java:comp/env/";
    private Context context;

    protected DatasourceHelper() {
        // use getInstance()
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



    public static DatasourceHelper getInstance() {
        return INSTANCE;
    }
    /**
     * Finds a datasource name property matching module (db[.module].jndi.name)
     * @param prefix module name like myplaces, analysis etc null means "core" oskari
     * @return defaults to jdbc/OskariPool if not configured
     */
    public String getOskariDataSourceName(final String prefix) {
        final String poolToken = (prefix == null) ? "" : prefix + ".";
        return PropertyUtil.get(PREFIX_DB + poolToken + "jndi.name", DEFAULT_DATASOURCE_NAME);
    }

    /**
     * Returns the default datasource (jdbc/OskariPool) from context.
     */
    public DataSource getDataSource() {
        return getDataSource(getContext(), getOskariDataSourceName());
    }
    /**
     * Returns a datasource matching the name from context.
     * @param name for example jdbc/OskariPool
     */
    public DataSource getDataSource(final String name) {
        return getDataSource(getContext(), name);
    }
    /**
     * Returns a datasource matching the name from the given context.
     * @param name for example jdbc/OskariPool
     */
    public DataSource getDataSource(final Context ctx, final String name) {
        if(ctx == null) {
            return null;
        }
        try {
            return (DataSource) ctx.lookup(JNDI_PREFIX + name);
        } catch (Exception ex) {
            LOGGER.info("Couldn't find pool with name '" + name + "': " + ex.getMessage());
        }
        return null;
    }


    public boolean checkDataSource(final Context ctx) {
        return checkDataSource(ctx, null);
    }

    /**
     * Ensures the datasource matching the module (prefix) is present in the given context.
     * Creates the datasource and binds it to context if not present.
     * @param ctx
     * @param prefix module like myplaces, analysis
     * @return
     */
    public boolean checkDataSource(final Context ctx, final String prefix) {
        final String poolToken = (prefix == null) ? "" : prefix + ".";
        final String poolName = getOskariDataSourceName(prefix);

        LOGGER.info(String.format(MSG_CHECKING_POOL, poolName));
        final DataSource ds = getDataSource(ctx, poolName);
        boolean success = ds != null;
        if(success) {
            // using container provided datasource rather than one created by us
            LOGGER.info("Found JNDI dataSource with name: " + poolName +
                    ". Using it instead of properties configuration db." + poolToken + "url");
        } else {
            LOGGER.info(" - creating a DataSource with defaults based on configured properties");
            final DataSource dataSource = createDataSource(prefix);
            addDataSource(ctx, poolName, dataSource);
            LOGGER.info(String.format(MSG_CHECKING_POOL, poolName));
            success = (getDataSource(ctx, poolName) != null);
        }
        return success;
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

    private void addDataSource(final Context ctx, final String name, final DataSource ds) {
        if(ctx == null) {
            return;
        }
        try {
            constructContext(ctx, "comp", "env", "jdbc");
            ctx.bind(JNDI_PREFIX + name, ds);
        } catch (Exception ex) {
            LOGGER.error(ex, "Couldn't add pool with name '" + name +"': ", ex.getMessage());
        }
    }

    /**
     * Constructs the context path if not available yet.
     * @param ctx
     * @param path
     */
    private void constructContext(final Context ctx, final String... path) {
        StringWriter current = new StringWriter();
        current.append("java:");
        for (String key : path) {
            try {
                current.append("/" + key);
                ctx.createSubcontext(current.toString());
            } catch (Exception ignored) {
                LOGGER.ignore("Ignore context creation fail", ignored);
            }
        }
    }

    /**
     * Creates an InitialContext if not created yet.
     */
    public Context getContext() {
        if(context == null) {
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
        for(BasicDataSource ds : localDataSources) {
            try {
                ds.close();
                LOGGER.debug("Closed locally created data source");
            } catch (final SQLException e) {
                LOGGER.error(e, "Failed to close locally created data source");
            }
        }
    }

}

package fi.nls.oskari.map.servlet;

import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.dbcp.BasicDataSource;

import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

/**
 * Initializes context for oskari-map servlet:
 * - Loads properties
 * - Checks database connections
 *
 * Prints status messages and tries to act nice even if runtime exception occurs.
 */
public class OskariContextInitializer implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // noop
        System.out.println("Context destroy");
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            // catch all so we don't get mysterious listener start errors
            initializeOskariContext();
        }
        catch (Exception ex) {
            error("!!! Error initializing context for Oskari !!!");
            ex.printStackTrace();
        }
    }

    /**
     * Main initialization method
     */
    private void initializeOskariContext() {

        info("#########################################################");
        info("Oskari-map context is being initialized");
        // populate properties
        info("- loading /oskari.properties");
        PropertyUtil.loadProperties("/oskari.properties");
        info("- loading /oskari-ext.properties");
        PropertyUtil.loadProperties("/oskari-ext.properties");

        info("- checking default DataSource");
        final InitialContext ctx = getContext();
        if(!checkDataSource(ctx, null)) {
            error("Couldn't initialize default DataSource");
        }

        // loop "db.additional.pools" to see if we need any more pools configured
        info("- checking additional DataSources");
        final String[] additionalPools = PropertyUtil.getCommaSeparatedList("db.additional.pools");
        for(String pool : additionalPools) {
            if(!checkDataSource(ctx, pool)) {
                error("Couldn't initialize DataSource with prefix: " + pool);
            }
        }
        // TODO: possibly update database structure if we start to use http://flywaydb.org/ or similar (or maybe in another listener)
        info("Oskari-map context initialization done");
        info("#########################################################");
    }

    private boolean checkDataSource(final InitialContext ctx, final String prefix) {
        final String poolToken = (prefix == null) ? "" : prefix + ".";
        final String poolName = PropertyUtil.get("db." + poolToken + "jndi.name", "jdbc/OskariPool");

        info(" - checking existance of database pool: " + poolName);
        final DataSource ds = getDataSource(ctx, poolName);
        boolean success = (ds != null);
        if(!success) {
            warn("!!! Couldn't find DataSource with name: " + poolName);
            warn("!!! Please edit webapps XML (web.xml/context.xml etc) to provide database connection resource !!!");
            //  + " - creating one with defaults."
            info(" - trying to create DataSource with defaults based on configured properties");
            final DataSource defaultPool = createDataSource(null);
            addDataSource(ctx, poolName, defaultPool);
            info(" - checking existance of database pool: " + poolName);
            success = (getDataSource(ctx, poolName) != null);
        }
        return success;
    }

    private InitialContext getContext() {
        try {
            return new InitialContext();
        } catch (Exception ex) {
            System.err.println("Couldn't get context: " + ex.getMessage());
        }
        return null;
    }

    private void addDataSource(final InitialContext ctx, final String name, final DataSource ds) {
        if(ctx == null) {
            return;
        }
        try {
            constructContext(ctx, "comp", "env", "jdbc");
            ctx.bind("java:comp/env/" + name, ds);
        } catch (Exception ex) {
            System.err.println("Couldn't add pool with name '" + name +"': " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void constructContext(final InitialContext ctx, final String... path) {
        String current = "java:";
        for(String key : path) {
            try {
                current = current + "/" + key;
                ctx.createSubcontext(current);
            } catch (Exception ignored) { }
        }
    }

    private DataSource getDataSource(final InitialContext ctx, final String name) {
        if(ctx == null) {
            return null;
        }
        try {
            return (DataSource) ctx.lookup("java:comp/env/" + name);
        } catch (Exception ex) {
            System.err.println("Couldn't find pool with name '" + name +"': " + ex.getMessage());
        }
        return null;
    }

    private DataSource createDataSource(final String prefix) {
        final String poolToken = (prefix == null) ? "" : prefix + ".";

        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(PropertyUtil.get("db.jndi.driverClassName", "org.postgresql.Driver"));
        dataSource.setUrl(PropertyUtil.get("db." + poolToken + "url", "jdbc:postgresql://localhost:5432/oskaridb"));
        dataSource.setUsername(PropertyUtil.get("db." + poolToken + "username", ""));
        dataSource.setPassword(PropertyUtil.get("db." + poolToken + "password", ""));
        return dataSource;
    }

    /*
    LOGGING METHODS
     */
    private void info(final String msg) {
        System.out.println("# " + msg);
    }
    private void warn(final String msg) {
        System.err.println("# !!! " + msg);
    }

    private void error(final String msg) {
        System.err.println("# !!!!!!!!!!!!!!!!!!!!!!!!!");
        System.err.println("# !!! " + msg);
        System.err.println("# !!!!!!!!!!!!!!!!!!!!!!!!!");
    }
}

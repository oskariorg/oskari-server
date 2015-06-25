package fi.nls.oskari.map.servlet;

import fi.nls.oskari.db.DBHandler;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.db.FlywaydbMigrator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.scheduler.SchedulerService;
import fi.nls.oskari.util.PropertyUtil;
import org.quartz.SchedulerException;

import javax.naming.Context;
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

    private final static Logger log = LogFactory.getLogger(OskariContextInitializer.class);

    private SchedulerService schedulerService;
    private final DatasourceHelper DS_HELPER = new DatasourceHelper();

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        try {
            this.schedulerService.shutdownScheduler();
        } catch (final SchedulerException e) {
            log.error(e, "Failed to shut down the Oskari scheduler");
        }
        DS_HELPER.teardown();
        info("Context destroy");
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        try {
            // catch all so we don't get mysterious listener start errors
            info("#########################################################");
            info("Oskari-map context is being initialized");
            initializeOskariContext();

            // create initial content if properties tells us to
            if("true".equals(PropertyUtil.getOptional("oskari.init.db"))) {
                info("- checking for initial db content");
                DBHandler.createContentIfNotCreated(DS_HELPER.getDataSource());
            }
            info("Oskari-map context initialization done");

            info("Oskari-map checking DB status");
            FlywaydbMigrator.migrate(DS_HELPER.getDataSource());
            info("core DB migrated");
            final String[] additionalPools = PropertyUtil.getCommaSeparatedList("db.additional.pools");
            for(String module : additionalPools) {
                final String poolName = DS_HELPER.getOskariDataSourceName(module);
                FlywaydbMigrator.migrate(DS_HELPER.getDataSource(poolName), module);
                info(module + " DB migrated");
            }
            info("#########################################################");
        }
        catch (Exception ex) {
            error("!!! Error initializing context for Oskari !!!");
            log.error(ex);
        }

        this.schedulerService = new SchedulerService();
        try {
            this.schedulerService.initializeScheduler();
        } catch (final SchedulerException e) {
            log.error(e, "Failed to start up the Oskari scheduler");
        }
    }

    /**
     * Main initialization method
     */
    private void initializeOskariContext() {

        loadProperties();

        info("- checking default DataSource");
        final Context ctx = DS_HELPER.getContext();
        if(!DS_HELPER.checkDataSource(ctx)) {
            error("Couldn't initialize default DataSource");
        }

        // loop "db.additional.pools" to see if we need any more pools configured
        info("- checking additional DataSources");
        final String[] additionalPools = PropertyUtil.getCommaSeparatedList("db.additional.pools");
        for(String pool : additionalPools) {
            if(!DS_HELPER.checkDataSource(ctx, pool)) {
                error("Couldn't initialize DataSource with prefix: " + pool);
            }
        }
        // TODO: possibly update database structure if we start to use http://flywaydb.org/ or similar (or maybe in another listener)
    }

    public void loadProperties() {
        // populate properties
        info("- loading /oskari.properties");
        PropertyUtil.loadProperties("/oskari.properties");
        info("- loading /oskari-ext.properties");
        PropertyUtil.loadProperties("/oskari-ext.properties");
    }

    private void info(final String msg) {
        log.info("# " + msg);
    }

    private void error(final String msg) {
        log.error("# !!!!!!!!!!!!!!!!!!!!!!!!!");
        log.error("# !!! " + msg);
        log.error("# !!!!!!!!!!!!!!!!!!!!!!!!!");
    }
}

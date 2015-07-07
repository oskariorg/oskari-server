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

    private static Logger log = LogFactory.getLogger(OskariContextInitializer.class);

    private SchedulerService schedulerService;
    private static final DatasourceHelper DS_HELPER = new DatasourceHelper();

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        try {
            this.schedulerService.shutdownScheduler();
        } catch (final SchedulerException e) {
            log.error(e, "Failed to shut down the Oskari scheduler");
        }
        DS_HELPER.teardown();
        log.info("Context destroy");
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        try {
            loadProperties();
            // init logger after the properties so we get the correct logger impl
            log = LogFactory.getLogger(OskariContextInitializer.class);
            // catch all so we don't get mysterious listener start errors
            log.info("#########################################################");
            log.info("Oskari-map context is being initialized");
            initializeOskariContext();

            // create initial content if properties tells us to
            if("true".equals(PropertyUtil.getOptional("oskari.init.db"))) {
                log.info("- checking for initial db content");
                DBHandler.createContentIfNotCreated(DS_HELPER.getDataSource());
            }
            log.info("Oskari-map context initialization done");

            migrateDB();
            log.info("#########################################################");
        }
        catch (Exception ex) {
            log.error(ex, "!!! Error initializing context for Oskari !!!");
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

        log.info("- checking default DataSource");
        final Context ctx = DS_HELPER.getContext();
        if(!DS_HELPER.checkDataSource(ctx)) {
            log.error("Couldn't initialize default DataSource");
        }

        // loop "db.additional.pools" to see if we need any more pools configured
        log.info("- checking additional DataSources");
        final String[] additionalPools = PropertyUtil.getCommaSeparatedList("db.additional.modules");
        for(String pool : additionalPools) {
            if(!DS_HELPER.checkDataSource(ctx, pool)) {
                log.error("Couldn't initialize DataSource for module:", pool);
            }
        }
    }

    private void migrateDB() {
        // upgrade database structure with http://flywaydb.org/
        log.info("Oskari-map checking DB status");
        try {
            FlywaydbMigrator.migrate(DS_HELPER.getDataSource());
            log.info("Oskari core DB migrated successfully");
        } catch (Exception e) {
            log.error(e, "DB migration for Oskari core failed!");
        }
        final String[] additionalPools = PropertyUtil.getCommaSeparatedList("db.additional.modules");
        for(String module : additionalPools) {
            final String poolName = DS_HELPER.getOskariDataSourceName(module);
            try {
                FlywaydbMigrator.migrate(DS_HELPER.getDataSource(poolName), module);
                log.info(module + " DB migrated successfully");
            } catch (Exception e) {
                log.error(e, "DB migration for module " + module + " failed!");
            }
        }
    }

    public void loadProperties() {
        // populate properties
        System.out.println("- loading /oskari.properties");
        PropertyUtil.loadProperties("/oskari.properties");
        System.out.println("- loading /oskari-ext.properties");
        PropertyUtil.loadProperties("/oskari-ext.properties");
    }
}

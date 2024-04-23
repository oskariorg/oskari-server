package org.oskari.init;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.oskari.helpers.FlywaydbMigrator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.scheduler.SchedulerService;
import fi.nls.oskari.util.PropertyUtil;
import org.quartz.SchedulerException;

import javax.naming.Context;

public class ServiceInitializer {

    private static final DatasourceHelper DS_HELPER = DatasourceHelper.getInstance();

    private static final String STR_LOG_LINE = "#########################################################";


    private static Logger LOG = LogFactory.getLogger(ServiceInitializer.class);
    private static SchedulerService schedulerService;
    private static boolean propsLoaded = false;

    private ServiceInitializer() {}

    public static void loadProperties() {
        // populate properties
        System.out.println("- loading /oskari.properties");
        PropertyUtil.loadProperties("/oskari.properties");
        System.out.println("- loading /oskari-ext.properties");
        PropertyUtil.loadProperties("/oskari-ext.properties");
        // init logger after the properties so we get the correct logger impl
        LOG = LogFactory.getLogger(ServiceInitializer.class);
        propsLoaded = true;
    }

    public static void init() {
        try {
            if(!propsLoaded) {
                loadProperties();
            }
            // catch all so we don't get mysterious listener start errors
            LOG.info(STR_LOG_LINE);
            LOG.info("Oskari-map context is being initialized");
            initializeOskariContext();

            // init jedis
            LOG.info("Initializing Redis connections");
            JedisManager.connect();
            LOG.info("Oskari-map context initialization done");
            LOG.info(STR_LOG_LINE);
        } catch (Exception ex) {
            LOG.error(ex, "!!! Error initializing context for Oskari !!!");
        }

        migrateDB();

        schedulerService = new SchedulerService();
        try {
            schedulerService.initializeScheduler();
        } catch (final SchedulerException e) {
            LOG.error(e, "Failed to start up the Oskari scheduler");
        }
    }

    /**
     * Main initialization method
     */
    public static void initializeOskariContext() {

        LOG.info("- checking default DataSource");
        final Context ctx = DS_HELPER.getContext();
        if(!DS_HELPER.checkDataSource(ctx)) {
            LOG.error("Couldn't initialize default DataSource");
        }

        // loop "db.additional.pools" to see if we need any more pools configured
        LOG.info("- checking additional DataSources");
        final String[] additionalPools = DatasourceHelper.getAdditionalModules();
        for(String pool : additionalPools) {
            if(!DS_HELPER.checkDataSource(ctx, pool)) {
                LOG.error("Couldn't initialize DataSource for module:", pool);
            }
        }
    }

    private static void migrateDB() {
        if (PropertyUtil.getOptional("db.flyway", true) == false) {
            LOG.warn("Skipping flyway migration! Remove 'db.flyway' property or set it to 'true' to enable migration");
            return;
        }
        boolean ignoreMigrationFailures = PropertyUtil.getOptional("db.ignoreMigrationFailures", false);
        CustomMigration customMigration = getCustomMigration();
        if (customMigration != null) {
            LOG.warn("Running custom migration instead of built-in");
            customMigration.migrateDB();
            return;
        }

        // upgrade database structure with http://flywaydb.org/
        LOG.info("Oskari-map checking DB status");
        try {
            FlywaydbMigrator.migrate(DS_HELPER.getDataSource());
            LOG.info("Oskari core DB migrated successfully");
        } catch (Exception e) {
            LOG.error(e, "DB migration for Oskari core failed!");
            if(!ignoreMigrationFailures) {
                throw e;
            }
        }
        final String[] additionalPools = DatasourceHelper.getAdditionalModules();
        for(String module : additionalPools) {
            final String poolName = DS_HELPER.getOskariDataSourceName(module);
            try {
                FlywaydbMigrator.migrate(DS_HELPER.getDataSource(poolName), module);
                LOG.info(module + " DB migrated successfully");
            } catch (Exception e) {
                LOG.error(e, "DB migration for module", module, "failed!");
                if(!ignoreMigrationFailures) {
                    throw e;
                }
            }
        }
    }

    public static CustomMigration getCustomMigration() {
        String className = PropertyUtil.getOptional("db.flyway.migrationCls");
        if (className == null) {
            return null;
        }
        try {
            final Class clazz = Class.forName(className);
            return (CustomMigration) clazz.newInstance();
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error initializing migration class from 'db.flyway.migrateCls='" + className, e);
        }
    }

    public static void teardown() {
        if (schedulerService != null) {
            try {
                schedulerService.shutdownScheduler();
            } catch (final SchedulerException e) {
                LOG.error(e, "Failed to shut down the Oskari scheduler");
            }
        }
        DS_HELPER.teardown();
        JedisManager.shutdown();
        LOG.info("Context destroy");
    }
}

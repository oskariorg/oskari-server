package fi.nls.oskari.servlet;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.db.DBHandler;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.db.FlywaydbMigrator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.scheduler.SchedulerService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.quartz.SchedulerException;

import javax.naming.Context;


/**
 * Created by SMAKINEN on 8.7.2015.
 */
public class WebappHelper {

    private static final DatasourceHelper DS_HELPER = DatasourceHelper.getInstance();
    private static final String KEY_REDIS_HOSTNAME = "redis.hostname";
    private static final String KEY_REDIS_PORT = "redis.port";
    private static final String KEY_REDIS_POOL_SIZE = "redis.pool.size";

    private static final String STR_LOG_LINE = "#########################################################";


    private static Logger log = LogFactory.getLogger(WebappHelper.class);
    private static SchedulerService schedulerService;
    private static boolean propsLoaded = false;

    private WebappHelper() {}

    public static void loadProperties() {
        // populate properties
        System.out.println("- loading /oskari.properties");
        PropertyUtil.loadProperties("/oskari.properties");
        System.out.println("- loading /oskari-ext.properties");
        PropertyUtil.loadProperties("/oskari-ext.properties");
        // init logger after the properties so we get the correct logger impl
        log = LogFactory.getLogger(WebappHelper.class);
        propsLoaded = true;
    }

    public static void init() {
        try {
            if(!propsLoaded) {
                loadProperties();
            }
            // catch all so we don't get mysterious listener start errors
            log.info(STR_LOG_LINE);
            log.info("Oskari-map context is being initialized");
            initializeOskariContext();

            // create initial content if properties tells us to
            if("true".equals(PropertyUtil.getOptional("oskari.init.db"))) {
                log.info("- checking for initial db content");
                DBHandler.createContentIfNotCreated(DS_HELPER.getDataSource());
            }

            migrateDB();

            // init jedis
            log.info("Initializing Redis connections");
            JedisManager.connect(
                    ConversionHelper.getInt(PropertyUtil.get(KEY_REDIS_POOL_SIZE), 30),
                    PropertyUtil.get(KEY_REDIS_HOSTNAME, "localhost"),
                    ConversionHelper.getInt(PropertyUtil.get(KEY_REDIS_PORT), 6379));
            log.info("Oskari-map context initialization done");
            log.info(STR_LOG_LINE);
        } catch (Exception ex) {
            log.error(ex, "!!! Error initializing context for Oskari !!!");
        }

        schedulerService = new SchedulerService();
        try {
            schedulerService.initializeScheduler();
        } catch (final SchedulerException e) {
            log.error(e, "Failed to start up the Oskari scheduler");
        }
    }

    /**
     * Main initialization method
     */
    public static void initializeOskariContext() {

        log.info("- checking default DataSource");
        final Context ctx = DS_HELPER.getContext();
        if(!DS_HELPER.checkDataSource(ctx)) {
            log.error("Couldn't initialize default DataSource");
        }

        // loop "db.additional.pools" to see if we need any more pools configured
        log.info("- checking additional DataSources");
        final String[] additionalPools = DS_HELPER.getAdditionalModules();
        for(String pool : additionalPools) {
            if(!DS_HELPER.checkDataSource(ctx, pool)) {
                log.error("Couldn't initialize DataSource for module:", pool);
            }
        }
    }

    private static void migrateDB() {
        if(PropertyUtil.getOptional("db.flyway", true) == false) {
            log.warn("Skipping flyway migration! Remove 'db.flyway' property or set it to 'true' to enable migration");
            return;
        }
        // upgrade database structure with http://flywaydb.org/
        log.info("Oskari-map checking DB status");
        try {
            FlywaydbMigrator.migrate(DS_HELPER.getDataSource());
            log.info("Oskari core DB migrated successfully");
        } catch (Exception e) {
            log.error(e, "DB migration for Oskari core failed!");
        }
        final String[] additionalPools = DS_HELPER.getAdditionalModules();
        for(String module : additionalPools) {
            final String poolName = DS_HELPER.getOskariDataSourceName(module);
            try {
                FlywaydbMigrator.migrate(DS_HELPER.getDataSource(poolName), module);
                log.info(module + " DB migrated successfully");
            } catch (Exception e) {
                log.error(e, "DB migration for module " + module + " failed!", e);
                e.printStackTrace();
            }
        }
    }

    public static void teardown() {
        if (schedulerService != null) {
            try {
                schedulerService.shutdownScheduler();
            } catch (final SchedulerException e) {
                log.error(e, "Failed to shut down the Oskari scheduler");
            }
        }
        DS_HELPER.teardown();
        JedisManager.shutdown();
        log.info("Context destroy");
    }
}

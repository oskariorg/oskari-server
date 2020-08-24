package fi.nls.oskari.db;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import javax.sql.DataSource;

/**
 * Created by SMAKINEN on 11.6.2015.
 */
public class FlywaydbMigrator {

    private static final Logger LOG = LogFactory.getLogger(FlywaydbMigrator.class);

    private static final String DEFAULT_STATUS_TABLE_NAME = "oskari_status";
    private static final String DEFAULT_SCRIPT_LOCATION = "/flyway/";

    private FlywaydbMigrator() {}

    public static void migrate(DataSource datasource) {
        migrate(datasource, null);
    }
    public static void migrate(DataSource datasource, final String moduleName) {
        Flyway flyway = getModuleMigration(datasource, moduleName);
        if (PropertyUtil.getOptional("db.flyway.autorepair", false)) {
            // https://github.com/flyway/flyway/issues/253
            flyway.repair();
        }
        flyway.migrate();
    }

    /**
     * Handles baselining for 1.x and 2.0 versions
     * @param datasource
     * @return
     */
    private static Flyway getCoreMigration(DataSource datasource) {
        FluentConfiguration config = Flyway.configure()
                .dataSource(datasource)
                .table(getStatusTableName(null))
                .locations(getScriptLocations(null))
                .baselineVersion("2.0.0");
        Flyway flyway = config.load();
        if (flyway.info().current() == null) {
            // empty database/fresh start
            flyway.baseline();
        } else {
            // check if we are updating an existing 1.x database
            String currentVersion = flyway.info().current().getVersion().getVersion();
            if ("1.55.7".equals(currentVersion)) {
                // 1.55.7 is the last migration for core module before 2.0 -> bump baseline to skip table creation
                // skip creating initial tables (migration 2.0.1)
                // skip registering initial bundleso (migration 2.0.2)
                // skip other internal data (migration 2.0.3)
                config.baselineVersion("2.0.4");
                flyway = config.load();
                flyway.baseline();
            } else if (currentVersion.startsWith("1.x")) {
                // handle Flyway deprecated version of status table?
                // version 1.56.0 is required as base for existing db as it updated Flyway/status table to modern format
                throw new RuntimeException("Migrate to Oskari version 1.56 before 2+");
            }
        }
        return flyway;
    }

    private static Flyway getModuleMigration(DataSource datasource, final String moduleName) {
        if (moduleName == null) {
            return getCoreMigration(datasource);
        }
        // myplaces/userlayer or application module (use the same baseline as with 1.x Oskari)
        Flyway flyway = Flyway.configure()
                .dataSource(datasource)
                .table(getStatusTableName(moduleName))
                .locations(getScriptLocations(moduleName))
                .baselineVersion("0.1")
                .load();

        if (flyway.info().current() == null) {
            flyway.baseline();
        }
        return flyway;
    }

    private static String[] getScriptLocations(final String prefix) {
        final String moduleName = (prefix == null) ? "oskari" : prefix;
        final String[] locations = PropertyUtil.getCommaSeparatedList("db." + normalizePrefix(prefix) + "script.locations");
        if(locations.length > 0) {
            return locations;
        }
        // default to flyway/[oskari | myplaces | otherModuleName]
        return new String[]{DEFAULT_SCRIPT_LOCATION + moduleName};
    }

    private static String getStatusTableName(final String prefix) {
        final String tableNamePart = (prefix == null) ? "" : "_" + prefix;
        // default to oskari[_moduleName]_status
        return PropertyUtil.get("db." + normalizePrefix(prefix) + "status_table", DEFAULT_STATUS_TABLE_NAME + tableNamePart);
    }

    private static String normalizePrefix(final String prefix) {
        if(prefix == null) {
            return "";
        }
        return prefix + ".";
    }

}

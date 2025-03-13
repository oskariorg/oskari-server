package org.oskari.helpers;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
        // This will throw expections for "referenced migrations that are unavailable" for any
        // pre-1.56 versions so we don't need to handle it.
        FluentConfiguration config = Flyway.configure()
                // By default, Flyway has this as false, which will skip migrations with invalid naming.
                // When set to true, flyway will fail the migration, and log the invalid filenames
                .validateMigrationNaming(PropertyUtil.getOptional("db.flyway.validateMigrationNaming", true))
                .dataSource(datasource)
                .table(getStatusTableName(null))
                .locations(getScriptLocations(null))
                .baselineVersion("2.0.0");
        Flyway flyway = config.load();
        if (flyway.info().current() == null) {
            // empty database/fresh start
            flyway.baseline();
        } else if ("1.55.7".equals(flyway.info().current().getVersion().getVersion())) {
            // 1.55.7 is the last migration for core module before 2.0 -> we are updating an existing 1.x database
            // Flyway doesn't allow re-baselining so we need to modify the DB manually before re-baselining...
            dropLegacyMigrations(datasource);
            // skip 2.0 empty db setup that are already present when migrating from 1.x:
            // - skip creating initial tables (migration 2.0.1)
            // - skip registering initial bundles (migration 2.0.2)
            // - skip other internal data (migration 2.0.3)
            config.baselineVersion("2.0.4");
            flyway = config.load();
            flyway.baseline();
        }
        return flyway;
    }

    // Flyway doesn't allow re-baselining so we need to modify the DB manually before calling baseline() again.
    // This drops the core status table to allow Flyway to proceed with creating 2.x baseline and
    // run future migrations based on that
    private static void dropLegacyMigrations(DataSource datasource) {
        String sql = "DROP TABLE " + getStatusTableName(null);
        try (Connection conn = datasource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to clear legacy migrations", e);
        }
    }

    private static Flyway getModuleMigration(DataSource datasource, final String moduleName) {
        if (moduleName == null) {
            return getCoreMigration(datasource);
        }
        // myplaces/userlayer or application module (use the same baseline as with 1.x Oskari)
        Flyway flyway = Flyway.configure()
                // By default, Flyway has this as false, which will skip migrations with invalid naming.
                // When set to true, flyway will fail the migration, and log the invalid filenames
                .validateMigrationNaming(PropertyUtil.getOptional("db.flyway.validateMigrationNaming", true))
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

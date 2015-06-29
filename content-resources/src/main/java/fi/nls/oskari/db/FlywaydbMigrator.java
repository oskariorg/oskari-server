package fi.nls.oskari.db;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

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
        Flyway flyway = new Flyway();
        flyway.setDataSource(datasource);
        flyway.setTable(getStatusTableName(moduleName));
        flyway.setLocations(getScriptLocations(moduleName));
        try {
            if (flyway.info().current() == null) {
                flyway.setBaselineVersionAsString("0.1");
                flyway.baseline();
            }
            flyway.migrate();
        } catch (final FlywayException e) {
            LOG.error(e, "Failed to migrate");
        }
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

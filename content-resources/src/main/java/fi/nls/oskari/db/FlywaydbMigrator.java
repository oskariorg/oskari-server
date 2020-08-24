package fi.nls.oskari.db;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.Flyway;

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
        Flyway flyway = Flyway.configure()
                .dataSource(datasource)
                .table(getStatusTableName(moduleName))
                .locations(getScriptLocations(moduleName))
                .baselineVersion("2.0.0")
                .load();
        if (flyway.info().current() == null) {
            // empty db -> 2.0.0
            // existing db -> pre-flyway migration and baseline to 2.0.1?
            /*
- kantadumppi kentistä alustaa taulut
- ainakin portti_bundleen pitäisi saada myös dataa (tarviiko muihin?)
- osan tauluista voisi uudelleennimetä, mutta miten sen tekisi olemassa olevalle kannalle?
- samoin esim portti_bundlessa on startup-kenttä mutta constraintilla että arvo on oltava null
- oskari_jaas_users -> oskari_users_credentials

- flywayn ohi "2.0 migration" joka tekee vanhasta kannasta samanlaisen joka muokatulla dumpilla alustettaisiin?
             */
            //flyway.getConfiguration().basesetsetBaselineVersionAsString("0.1");
            flyway.baseline();
        } else {
            // 2020-08-21 15:29:16,057 WARN  fi.nls.oskari.db.FlywaydbMigrator - Current schema version = 1.55.7
            // 2020-08-21 15:29:17,110 WARN  fi.nls.oskari.db.FlywaydbMigrator - Current schema version = 1.0.13
            LOG.warn("Current schema version =", flyway.info().current().getVersion().getVersion());
        }
        if(PropertyUtil.getOptional("db.flyway.autorepair", false)) {
            // https://github.com/flyway/flyway/issues/253
            flyway.repair();
        }
        flyway.migrate();
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

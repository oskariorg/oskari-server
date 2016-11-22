package flyway.sample;

import fi.nls.oskari.util.FlywayHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * Adds statsgrid bundle to default and user views.
 */
public class V1_0_8__add_statsgrid_to_default_views implements JdbcMigration {
    private static final String BUNDLE_ID = "statsgrid";

    public void migrate(Connection connection) throws Exception {
        if(PropertyUtil.getOptional("flyway.sample.1_0_8.skip", true)) {
            return;
        }
        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for(Long viewId : views){
            if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
        }
    }
}

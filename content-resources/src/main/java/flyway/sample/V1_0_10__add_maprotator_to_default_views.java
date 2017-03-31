package flyway.sample;

import fi.nls.oskari.util.FlywayHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * Adds maprotator bundle to default and user views.
 */
public class V1_0_10__add_maprotator_to_default_views implements JdbcMigration {
    private static final String BUNDLE_ID = "maprotator";

    public void migrate(Connection connection) throws Exception {
        if(PropertyUtil.getOptional("flyway.sample.1_0_10.skip", false)) {
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

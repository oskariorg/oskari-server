package flyway.sample;

import fi.nls.oskari.db.ViewHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.ArrayList;

/**
 * Adds statsgrid bundle to default and user views.
 */
public class V1_0_8__add_statsgrid_to_default_views implements JdbcMigration {
    private static final String BUNDLE_ID = "statsgrid";

    public void migrate(Connection connection) throws Exception {
        if(PropertyUtil.getOptional("flyway.sample.1_0_8.skip", false)) {
            return;
        }
        final ArrayList<Long> views = ViewHelper.getUserAndDefaultViewIds(connection);
        for(Long viewId : views){
            if (ViewHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            ViewHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
        }
    }
}

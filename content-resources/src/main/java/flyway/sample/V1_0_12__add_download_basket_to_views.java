package flyway.sample;

import fi.nls.oskari.util.FlywayHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * Migration is skipped by default!
 *
 * Add "flyway.sample.1_0_12.skip=false" in oskari-ext.properties to add
 * download-basket for all default and user views.
 *
 */
public class V1_0_12__add_download_basket_to_views implements JdbcMigration {

    private static final String BUNDLE_ID = "download-basket";

    public void migrate(Connection connection) throws Exception {
        if (PropertyUtil.getOptional("flyway.sample.1_0_12.skip", true)) {
            return;
        }

        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for (Long viewId : views) {
            if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
        }
    }
}

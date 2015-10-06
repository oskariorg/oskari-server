package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Checks if feature id config is already present in the db and inserts it if not
 */
public class V1_31_6__register_admin_and_metrics_bundles implements JdbcMigration {

    private static final String ADMIN = "admin";
    private static final String METRICS = "metrics";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered

        Bundle admin = new Bundle();
        admin.setName(ADMIN);
        admin.setStartup(BundleHelper.getDefaultBundleStartup(ADMIN, ADMIN, "Generic Admin"));
        BundleHelper.registerBundle(admin, connection);

        Bundle metrics = new Bundle();
        metrics.setName(METRICS);
        metrics.setStartup(BundleHelper.getDefaultBundleStartup(ADMIN, METRICS, "Admin metrics panel"));
        BundleHelper.registerBundle(metrics, connection);

    }
}

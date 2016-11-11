package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Checks if bundle is already present in the db and inserts it if not
 */
public class V1_40_3__register_feedback_bundle implements JdbcMigration {

    private static final String NAME = "feedbackService";
    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle admin = new Bundle();
        admin.setName(NAME);
        admin.setStartup(BundleHelper.getDefaultBundleStartup(null, NAME, "Open 311 service"));
        BundleHelper.registerBundle(admin, connection);
    }
}

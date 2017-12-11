package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

public class V1_45_2__register_admin_publish_transfer_bundle implements JdbcMigration {

    private static final String NAMESPACE = "framework";
    private static final String BUNDLE_ID = "admin-publish-transfer";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        bundle.setStartup(BundleHelper.getDefaultBundleStartup(NAMESPACE, BUNDLE_ID, "Published maps import/export"));
        BundleHelper.registerBundle(bundle, connection);
    }
}

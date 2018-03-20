package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

public class V1_46_1__register_admin_hierarchical_layerlist_bundle implements JdbcMigration {
    private static final String NAMESPACE = "admin";
    private static final String BUNDLE_ID = "admin-hierarchical-layerlist";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        bundle.setStartup(BundleHelper.getDefaultBundleStartup(NAMESPACE, BUNDLE_ID, "Hierarchical admin layerlist"));
        BundleHelper.registerBundle(bundle, connection);
    }
}

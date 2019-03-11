package flyway.oskari;

import fi.nls.oskari.db.BundleHelper_pre1_52;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

public class V1_47_1__register_myplaces3_bundle implements JdbcMigration {

    private static final String NAMESPACE = "framework";
    private static final String BUNDLE_ID = "myplaces3";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        bundle.setStartup(BundleHelper_pre1_52.getDefaultBundleStartup(NAMESPACE, BUNDLE_ID, "OL3+ my places"));
        BundleHelper_pre1_52.registerBundle(bundle, connection);
    }
}

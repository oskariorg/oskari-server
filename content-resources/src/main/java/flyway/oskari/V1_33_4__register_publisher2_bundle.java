package flyway.oskari;

import fi.nls.oskari.db.BundleHelper_pre1_52;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class V1_33_4__register_publisher2_bundle implements JdbcMigration {

    private static final String NAMESPACE = "framework";
    private static final String BUNDLE_ID = "publisher2";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        bundle.setStartup(BundleHelper_pre1_52.getDefaultBundleStartup(NAMESPACE, BUNDLE_ID, "Publisher"));
        BundleHelper_pre1_52.registerBundle(bundle, connection);

    }
}

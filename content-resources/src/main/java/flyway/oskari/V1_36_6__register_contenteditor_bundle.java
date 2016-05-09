package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

public class V1_36_6__register_contenteditor_bundle implements JdbcMigration {

    private static final String NAMESPACE = "tampere";
    private static final String BUNDLE_ID = "content-editor";
    private static final String BUNDLE_TITLE = "content-editor";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        bundle.setStartup(BundleHelper.getDefaultBundleStartup(NAMESPACE, BUNDLE_ID, BUNDLE_TITLE));
        BundleHelper.registerBundle(bundle, connection);
    }
}

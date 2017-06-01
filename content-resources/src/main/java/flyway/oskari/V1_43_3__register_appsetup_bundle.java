package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by MKUOSMANEN on 31.5.2017.
 */
public class V1_43_3__register_appsetup_bundle implements JdbcMigration {

    private static final String NAMESPACE = "admin";
    private static final String BUNDLE_ID = "appsetup";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        bundle.setStartup(BundleHelper.getDefaultBundleStartup(NAMESPACE, BUNDLE_ID, "Admin appsetup tab"));
        BundleHelper.registerBundle(bundle, connection);

    }
}

package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by MKUOSMANEN on 26.1.2016.
 */
public class V1_35_2__register_timeseries_bundle implements JdbcMigration {

    private static final String NAMESPACE = "framework";
    private static final String BUNDLE_ID = "timeseries";
    private static final String BUNDLE_TITLE = "timeseries";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        bundle.setStartup(BundleHelper.getDefaultBundleStartup(NAMESPACE, BUNDLE_ID, BUNDLE_TITLE));
        BundleHelper.registerBundle(bundle, connection);
    }
}

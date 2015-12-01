package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class V1_34_0__register_drawtools_bundles implements JdbcMigration {

    private static final String PATH = "/Oskari/packages/mapping/ol2/";
    private static final String BUNDLE_ID = "drawtools";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        bundle.setStartup(BundleHelper.getBundleStartup(PATH, BUNDLE_ID, "DrawTools"));
        BundleHelper.registerBundle(bundle, connection);
    }
}

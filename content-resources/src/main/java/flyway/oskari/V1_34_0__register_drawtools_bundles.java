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

    private static final String PATH = "/Oskari/packages/mapping/";
    private static final String BUNDLE_ID_OL2 = "drawtools_ol2";
    private static final String BUNDLE_ID_OL3 = "drawtools_ol3";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle_ol2 = new Bundle();
        bundle_ol2.setName(BUNDLE_ID_OL2);
        bundle_ol2.setStartup(BundleHelper.getBundleStartup(PATH, BUNDLE_ID_OL2, "DrawTools OL2"));
        BundleHelper.registerBundle(bundle_ol2, connection);

        Bundle bundle_ol3 = new Bundle();
        bundle_ol3.setName(BUNDLE_ID_OL3);
        bundle_ol3.setStartup(BundleHelper.getBundleStartup(PATH, BUNDLE_ID_OL3, "DrawTools OL3"));
        BundleHelper.registerBundle(bundle_ol3, connection);
    }
}

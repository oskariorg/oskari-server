package flyway.oskari;

import java.sql.Connection;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;

public class V1_55_1__register_shadow_plugin_bundle implements JdbcMigration {

    private static final String BUNDLE_ID = "shadow-plugin-3d";

    @Override
    public void migrate(Connection connection) throws Exception {
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        BundleHelper.registerBundle(bundle, connection);
    }
}

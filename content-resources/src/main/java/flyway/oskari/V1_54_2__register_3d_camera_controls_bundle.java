package flyway.oskari;

import java.sql.Connection;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;

public class V1_54_2__register_3d_camera_controls_bundle implements JdbcMigration {

	private static final String BUNDLE_ID = "camera-controls-3d";

	@Override
	public void migrate(Connection connection) throws Exception {
		
		// BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        BundleHelper.registerBundle(bundle, connection);
	}
}

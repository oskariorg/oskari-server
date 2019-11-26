package flyway.oskari;

import java.sql.Connection;
import java.util.List;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.FlywayHelper;

public class V1_54_2__create_3d_camera_controls_plugin_and_add_it_to_3D_views implements JdbcMigration {

	private static final String BUNDLE_ID = "camera-controls-3d";

	@Override
	public void migrate(Connection connection) throws Exception {
		
		// BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        BundleHelper.registerBundle(bundle, connection);
		
		final List<Long> views = FlywayHelper.get3DApplicationViewIds(connection);
		for (Long viewId : views) {
			if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
				continue;
			}
			FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
		}
	}
}

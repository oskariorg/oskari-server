package flyway.oskari;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;

public class V2_3_0__register_layerswipe_bundle extends BaseJavaMigration {

    @Override
	public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
		
		// BundleHelper checks if these bundles are already registered
        BundleHelper.registerBundle(connection, "layerswipe");
	}
}

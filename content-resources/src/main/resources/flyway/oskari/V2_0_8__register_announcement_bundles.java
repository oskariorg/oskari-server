package flyway.oskari;

import java.sql.Connection;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import org.oskari.helpers.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;

public class V2_0_8__register_announcement_bundles extends BaseJavaMigration {

    @Override
	public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
		
		// BundleHelper checks if these bundles are already registered
        BundleHelper.registerBundle(connection, "announcements");
        BundleHelper.registerBundle(connection, "admin-announcements");
	}
}

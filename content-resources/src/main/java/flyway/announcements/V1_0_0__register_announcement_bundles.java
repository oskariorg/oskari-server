package flyway.announcements;

import java.sql.Connection;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import org.oskari.helpers.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;

public class V1_0_0__register_announcement_bundles extends BaseJavaMigration {

    @Override
	public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
		
		// BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName("announcements");
        BundleHelper.registerBundle(connection, bundle);

        Bundle admin = new Bundle();
        admin.setName("admin-announcements");
        BundleHelper.registerBundle(connection, admin);
	}
}

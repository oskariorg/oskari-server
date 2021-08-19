package flyway.oskari;

import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;

public class V2_5_0__register_and_add_userstyle_bundle extends BaseJavaMigration {

    @Override
	public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        BundleHelper.registerBundle(connection, "userstyle");
        AppSetupHelper.addBundleToApps(connection, new Bundle("userstyle"));
	}
}

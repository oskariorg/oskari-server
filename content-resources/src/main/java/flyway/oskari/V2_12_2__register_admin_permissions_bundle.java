package flyway.oskari;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;

/**
 * Register admin-permissions bundle (React impl) as intended replacement for admin-layerrights bundle (jQuery impl).
 */
public class V2_12_2__register_admin_permissions_bundle extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        BundleHelper.registerBundle(connection, "admin-permissions");
    }
}

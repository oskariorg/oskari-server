package flyway.oskari;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;

/**
 * Register layeranalytics and admin-layeranalytics bundles enabling tracking issues
 *  with map layer configurations based on end-user experience
 */
public class V2_7_2__register_layeranalytics_bundles extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        BundleHelper.registerBundle(connection, "layeranalytics");
        BundleHelper.registerBundle(connection, "admin-layeranalytics");
    }
}

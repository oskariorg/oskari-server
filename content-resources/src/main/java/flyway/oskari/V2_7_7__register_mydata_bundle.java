package flyway.oskari;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;

/**
 * Register mydata bundle as intended replacement for personaldata bundle.
 */
public class V2_7_7__register_mydata_bundle extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        BundleHelper.registerBundle(connection, "mydata");
    }
}

package flyway.oskari;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;

/**
 * Register metadatasearch bundle (React impl) as intended replacement for metadatacatalogue bundle (jQuery impl).
 */
public class V2_13_1__register_metadatasearch_bundle extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        BundleHelper.registerBundle(connection, "metadatasearch");
    }
}

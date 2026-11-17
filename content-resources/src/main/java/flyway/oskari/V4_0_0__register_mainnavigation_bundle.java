package flyway.oskari;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;

public class V4_0_0__register_mainnavigation_bundle extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        BundleHelper.registerBundle(context.getConnection(), "mainnavigation");
    }
}

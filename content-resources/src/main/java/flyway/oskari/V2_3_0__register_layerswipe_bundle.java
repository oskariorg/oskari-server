package flyway.oskari;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;

public class V2_3_0__register_layerswipe_bundle extends BaseJavaMigration {

    @Override
	public void migrate(Context context) throws Exception {
        BundleHelper.registerBundle(context.getConnection(), "layerswipe");
	}
}

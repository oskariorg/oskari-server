package flyway.myplaces;

import org.oskari.usercontent.UserDataLayerPopulator;
import org.oskari.usercontent.LayerHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V1_0_4__add_baselayer extends BaseJavaMigration {

    private static final String NAME = "oskari:my_places";

    public void migrate(Context ignored) throws Exception {
        if (LayerHelper.getLayerWithName(NAME) != null) {
            // already has base layer
            return;
        }
        UserDataLayerPopulator.setupMyplacesLayer(PropertyUtil.get("oskari.native.srs", "EPSG:4326"));
    }
}

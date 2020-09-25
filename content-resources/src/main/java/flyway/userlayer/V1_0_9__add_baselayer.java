package flyway.userlayer;

import org.oskari.usercontent.GeoserverPopulator;
import org.oskari.usercontent.LayerHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V1_0_9__add_baselayer extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_9__add_baselayer.class);

    private static final String NAME = "oskari:vuser_layer_data";

    public void migrate(Context ignored) throws Exception {
        if (LayerHelper.getLayerWithName(NAME) != null) {
            // already has base layer
            return;
        }
        GeoserverPopulator.setupUserLayer(PropertyUtil.get("oskari.native.srs", "EPSG:4326"));
    }
}

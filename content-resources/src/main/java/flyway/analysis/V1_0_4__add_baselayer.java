package flyway.analysis;

import fi.nls.oskari.geoserver.GeoserverPopulator;
import fi.nls.oskari.geoserver.LayerHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;

public class V1_0_4__add_baselayer extends BaseJavaMigration {

    private static final String NAME = "oskari:analysis_data";

    public void migrate(Context context) throws Exception {
        if (LayerHelper.getLayerWithName(NAME) != null) {
            // already has base layer
            return;
        }
        GeoserverPopulator.setupAnalysisLayer(PropertyUtil.get("oskari.native.srs", "EPSG:4326"));
    }
}

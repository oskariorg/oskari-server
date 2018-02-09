package flyway.analysis;

import fi.nls.oskari.geoserver.GeoserverPopulator;
import fi.nls.oskari.geoserver.LayerHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

public class V1_0_4__add_baselayer implements JdbcMigration {

    private static final String NAME = "oskari:analysis_data";

    public void migrate(Connection connection) throws Exception {
        if (LayerHelper.getLayerWithName(NAME) != null) {
            // already has base layer
            return;
        }
        GeoserverPopulator.setupAnalysisLayer(PropertyUtil.get("oskari.native.srs", "EPSG:4326"));
    }
}

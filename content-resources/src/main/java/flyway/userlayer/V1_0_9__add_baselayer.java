package flyway.userlayer;

import fi.nls.oskari.geoserver.GeoserverPopulator;
import fi.nls.oskari.geoserver.LayerHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

public class V1_0_9__add_baselayer implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_9__add_baselayer.class);

    private static final String NAME = "oskari:vuser_layer_data";

    public void migrate(Connection connection) throws Exception {
        if (LayerHelper.getLayerWithName(NAME) != null) {
            // already has base layer
            return;
        }
        GeoserverPopulator.setupUserLayer(PropertyUtil.get("oskari.native.srs", "EPSG:4326"));
    }
}

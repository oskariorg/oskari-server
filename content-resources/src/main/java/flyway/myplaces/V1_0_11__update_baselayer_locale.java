package flyway.myplaces;

import fi.nls.oskari.domain.map.OskariLayer;

import fi.nls.oskari.geoserver.LayerHelper;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import java.sql.Connection;

public class V1_0_11__update_baselayer_locale implements JdbcMigration {

    private static final String NAME = "oskari:my_places";
    private static final String LOCALE = "{fi:{name:\"Oma karttataso\"},sv:{name:\"Mitt kartlager\"},en:{name:\"My map layer\"},is:{name:\"Kortalagið mitt\"}}";
    public void migrate(Connection connection) throws Exception {
        OskariLayer layer = LayerHelper.getLayerWithName(NAME);
        layer.setLocale(JSONHelper.createJSONObject(LOCALE));
        LayerHelper.update(layer);
    }
}

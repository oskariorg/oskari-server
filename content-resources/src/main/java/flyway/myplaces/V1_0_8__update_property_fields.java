package flyway.myplaces;

import fi.nls.oskari.db.DBHandler;
import fi.nls.oskari.geoserver.GeoserverPopulator;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class V1_0_8__update_property_fields implements JdbcMigration {

    public void migrate(Connection ignored) throws Exception {
        // myplaces _can_ use other db than the default one
        // -> Use connection to default db for this migration
        Connection conn = DBHandler.getConnection();
        JSONObject attributes = GeoserverPopulator.addMyplacesAttributes(GeoserverPopulator.createUserContentAttributes());
        final String sql = "update oskari_maplayer set attributes =? where name = 'oskari:my_places';";

        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, attributes.toString());
            statement.execute();
        }
    }

}

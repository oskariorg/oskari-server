package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A layer specific JSON is now saved to db to get matrix sets etc directly for openlayers.
 * This updates the oskari_maplayer.capabilities column in the db
 */
public class V2_7_0__update_WMTS_capabilities extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        // NO-OP since 2.7.9 updates wmts-layers
    }

    protected void updateCapabilities(Connection conn, int layerId, JSONObject capabilities) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET capabilities=?, capabilities_last_updated=? where id=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, capabilities.toString());
            statement.setDate(2, new Date(System.currentTimeMillis()));
            statement.setInt(3, layerId);
            statement.execute();
        }
    }

    protected List<OskariLayer> getLayers(Connection conn, String type) throws SQLException {
        List<OskariLayer> layers = new ArrayList<>();
        final String sql = "SELECT id, url, name, username, password FROM oskari_maplayer where type = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, type);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    OskariLayer layer = new OskariLayer();
                    layer.setId(rs.getInt("id"));
                    layer.setType(type);
                    layer.setUrl(rs.getString("url"));
                    layer.setName(rs.getString("name"));
                    layer.setUsername(rs.getString("username"));
                    layer.setPassword(rs.getString("password"));
                    layers.add(layer);
                }
            }
        }
        return layers;
    }

}

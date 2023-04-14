package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Removes urlTemplate and requestEncoding from layer options.
// These were previously used to store the url from capabilities, but now we just use it from capabilities directly.
public class V2_11_3__clean_wmts_options extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Logger log = LogFactory.getLogger(V2_11_3__clean_wmts_options.class);
        Connection connection = context.getConnection();
        List<OskariLayer> layers = getLayers(connection, OskariLayer.TYPE_WMTS);
        for (OskariLayer layer : layers) {
            JSONObject options = layer.getOptions();
            if (options == null) {
                continue;
            }
            try {
                Object removedValueUrl = options.remove("urlTemplate");
                Object removedValueEnc = options.remove("requestEncoding");
                if (removedValueUrl != null || removedValueEnc != null) {
                    // if something was removed -> update db
                    updateOptions(connection, layer.getId(), options);
                }
            } catch (Exception e) {
                log.warn("Error updating options to db for layer:", layer.getId(), "options:", options);
            }
        }
    }

    protected List<OskariLayer> getLayers(Connection conn, String type) throws SQLException {
        List<OskariLayer> layers = new ArrayList<>();
        final String sql = "SELECT id, options FROM oskari_maplayer where type = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, type);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    OskariLayer layer = new OskariLayer();
                    layer.setId(rs.getInt("id"));
                    layer.setType(type);
                    layer.setOptions(JSONHelper.createJSONObject(rs.getString("options")));
                    layers.add(layer);
                }
            }
        }
        return layers;
    }

    protected void updateOptions(Connection conn, int layerId, JSONObject options) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET options=? where id=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, options.toString());
            statement.setInt(2, layerId);
            statement.execute();
        }
    }
}

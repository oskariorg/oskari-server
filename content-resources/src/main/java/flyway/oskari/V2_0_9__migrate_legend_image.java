package flyway.oskari;

import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class V2_0_9__migrate_legend_image extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<LayerConfig> layers = getLayers(connection);
        for (LayerConfig layer : layers) {
            migrateLegendImages(layer);
            updateOptions(connection, layer.layerId, layer.options);
        }

    }
    private void migrateLegendImages (LayerConfig layer) throws JSONException {
        String legendImage = layer.legendImage;
        JSONArray styleList = JSONHelper.getEmptyIfNull(layer.capabilities.optJSONArray("styles"));
        JSONObject legends = new JSONObject();
        if (styleList == null || styleList.length() == 0) {
            JSONHelper.putValue(legends, "legendImage", legendImage);
        } else {
            for (int i = 0; i < styleList.length(); i++ ) {
                JSONObject style = styleList.getJSONObject(i);
                // copy legend image to every style
                JSONHelper.putValue(legends, style.getString("name"), legendImage);
            }
        }
        JSONHelper.putValue(layer.options, "legends", legends);

    }
    private void updateOptions (Connection conn, int layerId, JSONObject options) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET options=? where id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, options.toString());
            statement.setInt(2, layerId);
            statement.execute();
        }
    }
    private List<LayerConfig> getLayers(Connection conn) throws SQLException {
        List<LayerConfig> layers = new ArrayList<>();
        // only process layers which haves legend_image (mostly wms and wmts layers)
        final String sql = "SELECT id, legend_image, options, capabilities FROM oskari_maplayer where legend_image <> '' and legend_image is not null";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    LayerConfig layer = new LayerConfig();
                    layer.layerId = rs.getInt("id");
                    layer.legendImage = rs.getString("legend_image");
                    layer.options = JSONHelper.createJSONObject(rs.getString("options"));
                    layer.capabilities = JSONHelper.createJSONObject(rs.getString("capabilities"));
                    layers.add(layer);
                }
            }
        }
        return layers;
    }
    class LayerConfig {
        int layerId;
        String legendImage;
        JSONObject options;
        JSONObject capabilities;
    }
}

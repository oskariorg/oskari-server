package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A layer specific JSON is now saved to db to get matrix sets etc directly for openlayers.
 * This updates the oskari_maplayer.capabilities column in the db
 */
public class V2_5_1__update_WMTS_capabilities extends BaseJavaMigration {

    private Map<String, String> capabilitiesXML = new HashMap<>();

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<LayerConfig> layers = getLayers(connection);
        for (LayerConfig layer : layers) {
            try {
                populateCapabilitiesJSON(layer);
                updateCapabilities(connection, layer.layerId, layer.capabilities);
            } catch (Exception e) {
                // lets skip layers that can't be updated at this point
                // otherwise the app will not start
                // the layers will not work before they are manually updated with admin or the scheduled update runs
                LogFactory.getLogger(V2_5_1__update_WMTS_capabilities.class).warn(
                        "Problem updating capabilities for layer in url:", layer.url,
                        "\n name: ", layer.name,
                        "\n id: ", layer.layerId);
            }
        }
        // remove capabilities from memory since they aren't used outside the migration
        capabilitiesXML.clear();
    }

    private void populateCapabilitiesJSON(LayerConfig layer) throws Exception {
        String data = getData(layer);
        WMTSCapabilities caps = WMTSCapabilitiesParser.parseCapabilities(data);
        JSONObject jscaps = LayerJSONFormatterWMTS.createCapabilitiesJSON(caps.getLayer(layer.name), null);
        layer.capabilities = jscaps;
    }

    private String getData(LayerConfig layer) throws Exception {
        String data = capabilitiesXML.get(layer.url);
        if (data != null) {
            return data;
        }
        return CapabilitiesCacheService.getFromService(layer.url, "wmtslayer",
                "1.0.0", layer.user, layer.pass);
    }

    private void updateCapabilities(Connection conn, int layerId, JSONObject capabilities) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET capabilities=?, capabilities_last_updated=? where id=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, capabilities.toString());
            statement.setDate(2, new Date(System.currentTimeMillis()));
            statement.setInt(3, layerId);
            statement.execute();
        }
    }

    private List<LayerConfig> getLayers(Connection conn) throws SQLException {
        List<LayerConfig> layers = new ArrayList<>();
        // only process layers which haves legend_image (mostly wms and wmts layers)
        final String sql = "SELECT id, url, name, username, password FROM oskari_maplayer where type = 'wmtslayer' order by url, name";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    LayerConfig layer = new LayerConfig();
                    layer.layerId = rs.getInt("id");
                    layer.url = rs.getString("url");
                    layer.name = rs.getString("name");
                    layer.user = rs.getString("username");
                    layer.pass = rs.getString("password");
                    layers.add(layer);
                }
            }
        }
        return layers;
    }

    class LayerConfig {
        int layerId;
        String url;
        String name;
        String user;
        String pass;
        JSONObject capabilities;
    }
}

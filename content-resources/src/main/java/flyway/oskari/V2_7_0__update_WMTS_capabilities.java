package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.service.OskariComponentManager;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.CapabilitiesUpdateResult;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * A layer specific JSON is now saved to db to get matrix sets etc directly for openlayers.
 * This updates the oskari_maplayer.capabilities column in the db
 */
public class V2_7_0__update_WMTS_capabilities extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Logger log = LogFactory.getLogger(V2_7_0__update_WMTS_capabilities.class);
        Connection connection = context.getConnection();
        List<OskariLayer> layers = getLayers(connection, OskariLayer.TYPE_WMTS);
        Set<String> systemCRS = ViewHelper.getSystemCRSs(OskariComponentManager.getComponentOfType(ViewService.class));
        List<CapabilitiesUpdateResult> results = CapabilitiesService.updateCapabilities(layers, systemCRS);

        Map<String, OskariLayer> layersById = new HashMap<>(layers.size());
        layers.forEach(l -> {
            layersById.put(Integer.toString(l.getId()), l);
        });
        for (CapabilitiesUpdateResult res : results) {
            if (res.getErrorMessage() != null) {
                log.warn( "Capabilities update error for layer:", res.getLayerId(), " - Error:", res.getErrorMessage());
                continue;
            }
            layers.stream()
                    .filter(l -> res.getLayerId().equals(Integer.toString(l.getId())))
                    .forEach(l -> {
                        try {
                            updateCapabilities(connection, l.getId(), l.getCapabilities());
                        } catch (SQLException e) {
                            log.warn(e, "Error updating db");
                        }
                    });
        }
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

    private List<OskariLayer> getLayers(Connection conn, String type) throws SQLException {
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

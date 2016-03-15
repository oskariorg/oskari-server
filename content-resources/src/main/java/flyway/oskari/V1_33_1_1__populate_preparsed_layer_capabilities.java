package flyway.oskari;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class V1_33_1_1__populate_preparsed_layer_capabilities implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_33_1_1__populate_preparsed_layer_capabilities.class);

    public void migrate(Connection connection)
            throws SQLException {
        List<OskariLayer> layers = getLayers(connection);

        LOG.info("Start generating prepopulated capabilities for WMS layers - count:", layers.size());
        int progress = 0;
        for (OskariLayer layer : layers) {
            try {
                final String url = layer.getSimplifiedUrl(true);
                final String xml = getCaps(url, connection);
                if (xml == null) {
                    LOG.info("Couldn't load capabilities for service:", url);
                    continue;
                }
                WebMapService wms = WebMapServiceFactory.createFromXML(layer.getName(), xml);
                if (wms == null) {
                    LOG.info("Couldn't parse capabilities for service:", url);
                    continue;
                }
                JSONObject capabilities = LayerJSONFormatterWMS.createCapabilitiesJSON(wms);
                if (capabilities != null) {
                    updateLayerCaps(layer.getId(), capabilities, connection);
                }
                progress++;
                LOG.info("Capabilities parsed:", progress, "/", layers.size());
            } catch (Exception e) {
                LOG.error(e, "Error getting capabilities for layer", layer);
            }
        }
    }

    private void updateLayerCaps(int layerId, JSONObject capabilities, Connection conn)
            throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET capabilities=? where id=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, capabilities.toString(2));
            statement.setInt(2, layerId);
            statement.execute();
        } catch (JSONException ignored) {
        }
    }

    String getCaps(String url, Connection conn)
            throws SQLException {
        // only process wms-layers
        final String sql = "SELECT data FROM oskari_capabilities_cache WHERE layertype='wmslayer' AND url=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, url);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("data");
                }
                return null;
            }
        }
    }

    List<OskariLayer> getLayers(Connection conn)
            throws SQLException {
        List<OskariLayer> layers = new ArrayList<>();
        // only process wms-layers
        final String sql = "SELECT id, url, type, name FROM oskari_maplayer where type='wmslayer'";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    OskariLayer layer = new OskariLayer();
                    layer.setId(rs.getInt("id"));
                    layer.setUrl(rs.getString("url"));
                    layer.setName(rs.getString("name"));
                    layer.setType(rs.getString("type"));
                    layers.add(layer);
                }
            }
        }
        return layers;
    }
}

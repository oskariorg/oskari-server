package flyway.oskari;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.wfs.GetGtWFSCapabilities;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Generates resource URL information for WMTS layers
 */
public class V1_35_5__update_layer_WMTS_matrixes implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_35_5__update_layer_WMTS_matrixes.class);

    private static final CapabilitiesCacheService CAPABILITIES_SERVICE = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);
    private static final WMTSCapabilitiesParser WMTSPARSER = new WMTSCapabilitiesParser();

    public void migrate(Connection connection) throws SQLException {

        List<OskariLayer> layers = getLayers(connection);
        LOG.info("Start populating matrixies for Oskari WMTS layers - count:", layers.size());
        int progress = 0;

        for (OskariLayer layer : layers) {
                    populateWMTS(connection, layer);
            progress++;
            LOG.info("WMTS layer matrix populate in process: layer: ", layer.getId()," - type:",layer.getType(),"---", progress, "/", layers.size());

        }
    }

    private void populateWMTS(Connection connection, OskariLayer layer) {
        try {
            // update
            OskariLayerCapabilities caps = CAPABILITIES_SERVICE.getCapabilities(layer);
            if (caps != null) {
                WMTSCapabilities parsed = WMTSPARSER.parseCapabilities(caps.getData());
                if (parsed != null) {
                    WMTSCapabilitiesLayer capsLayer = parsed.getLayer(layer.getName());
                    if (capsLayer != null) {
                        JSONObject jscaps = LayerJSONFormatterWMTS.createCapabilitiesJSON(parsed, capsLayer);
                        if (jscaps != null) {
                            updateCapabilities(layer.getId(), jscaps, connection);
                        }else {
                            LOG.info("WMTSCapabilities tilematrixes / json create failed - layer: ", layer.getName());
                        }
                    } else {
                        LOG.info("WMTSCapabilities tilematrixes / layer parse failed - layer: ", layer.getName());
                    }
                } else {
                    LOG.info("WMTSCapabilities tilematrixes / Capabilities parse failed - layer: ", layer.getName());
                }
            } else {
                LOG.info("WMTSCapabilities tilematrixes / getCapabilities failed - layer: ", layer.getName());
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting capabilities for layer", layer);
        }
    }


    private void updateCapabilities(int layerId, JSONObject capabilities, Connection conn) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET capabilities=? where id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, capabilities.toString(2));
            statement.setInt(2, layerId);
            statement.execute();
            statement.close();
        }
        catch (JSONException ignored) {
            LOG.error("Error updating oskari_maplayer.capabilities", layerId);
        }
    }


    private List<OskariLayer> getLayers(Connection conn) throws SQLException {
        List<OskariLayer> layers = new ArrayList<>();
        // only process wmts-layers
        final String sql = "SELECT id, name, url, type, username, password, srs_name, version FROM oskari_maplayer WHERE type='wmtslayer'";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    OskariLayer layer = new OskariLayer();
                    layer.setId(rs.getInt("id"));
                    layer.setName(rs.getString("name"));
                    layer.setUrl(rs.getString("url"));
                    layer.setType(rs.getString("type"));
                    layer.setUsername(rs.getString("username"));
                    layer.setPassword(rs.getString("password"));
                    layer.setSrs_name(rs.getString("srs_name"));
                    layer.setVersion(rs.getString("version"));
                    layers.add(layer);
                }
            }
        }
        return layers;
    }
}

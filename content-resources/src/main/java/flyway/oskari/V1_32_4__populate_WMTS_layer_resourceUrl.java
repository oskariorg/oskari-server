package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.ResourceUrl;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
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
 * Generates resource URL information for WMTS layers
 */
public class V1_32_4__populate_WMTS_layer_resourceUrl implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_32_4__populate_WMTS_layer_resourceUrl.class);

    private static final CapabilitiesCacheService CAPABILITIES_SERVICE = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);
    private static final WMTSCapabilitiesParser PARSER = new WMTSCapabilitiesParser();

    public void migrate(Connection connection) throws SQLException {
        List<OskariLayer> layers = getLayers(connection);

        LOG.info("Start generating resource URLS for WMTS layers - count:", layers.size());
        int progress = 0;
        for(OskariLayer layer : layers) {
            try {
                // update
                OskariLayerCapabilities caps = CAPABILITIES_SERVICE.getCapabilities(layer);
                WMTSCapabilities parsed = PARSER.parseCapabilities(caps.getData());
                WMTSCapabilitiesLayer capsLayer = parsed.getLayer(layer.getName());
                ResourceUrl resUrl = capsLayer.getResourceUrlByType("tile");
                if(resUrl != null) {
                    JSONHelper.putValue(layer.getOptions(), "requestEncoding", "REST");
                    JSONHelper.putValue(layer.getOptions(), "format", resUrl.getFormat());
                    JSONHelper.putValue(layer.getOptions(), "urlTemplate", resUrl.getTemplate());
                    updateOptions(layer.getId(), layer.getOptions(), connection);
                }
                progress++;
                LOG.info("Capabilities populated:", progress, "/", layers.size());
            } catch (Exception e) {
                LOG.error(e, "Error getting capabilities for layer", layer);
            }
        }
    }

    private void updateOptions(int layerId, JSONObject options, Connection conn) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET options=? where id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, options.toString(2));
            statement.setInt(2, layerId);
            statement.execute();
        }
        catch (JSONException ignored) {}
    }

    private List<OskariLayer> getLayers(Connection conn) throws SQLException {
        List<OskariLayer> layers = new ArrayList<>();
        // only process wmts-layers
        final String sql = "SELECT id, url, type FROM oskari_maplayer where type='wmtslayer'";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    OskariLayer layer = new OskariLayer();
                    layer.setId(rs.getInt("id"));
                    layer.setUrl(rs.getString("url"));
                    layer.setType(rs.getString("type"));
                    layers.add(layer);
                }
            }
        }
        return layers;
    }
}

package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class V1_32_1__populate_capabilities_cache implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_32_1__populate_capabilities_cache.class);

    private static final CapabilitiesCacheService CAPABILITIES_SERVICE = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);

    public void migrate(Connection connection) throws SQLException {
        List<OskariLayer> layers = getLayers(connection);

        LOG.info("Start populating capabilities for layers - count:", layers.size());
        Set<String> keys = new HashSet<>();
        int progress = 0;
        for(OskariLayer layer : layers) {
            final String layerKey = (layer.getSimplifiedUrl(true) + "----" + layer.getType()).toLowerCase();
            if(keys.contains(layerKey)) {
                progress++;
                continue;
            }
            keys.add(layerKey);
            final OskariLayerCapabilities caps = new OskariLayerCapabilities.Builder()
                .url(layer.getSimplifiedUrl(true))
                .layertype(layer.getType())
                .version(layer.getVersion())
                .data(CAPABILITIES_SERVICE.loadCapabilitiesFromService(layer, null))
                .build();
            insertCaps(connection, caps);
            progress++;
            LOG.info("Capabilities populated:", progress, "/", layers.size());
        }
    }

    private List<OskariLayer> getLayers(Connection conn) throws SQLException {
        List<OskariLayer> layers = new ArrayList<>();
        final String sql = "SELECT url, type, username, password FROM oskari_maplayer";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    OskariLayer layer = new OskariLayer();
                    layer.setUrl(rs.getString("url"));
                    layer.setType(rs.getString("type"));
                    layer.setUsername(rs.getString("username"));
                    layer.setPassword(rs.getString("password"));
                    layers.add(layer);
                }
            }
        }
        return layers;
    }

    private void insertCaps(Connection conn, OskariLayerCapabilities caps) throws SQLException {
        final String sql = "INSERT INTO oskari_capabilities_cache (layertype, url, data) VALUES(?,?,?)";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, caps.getLayertype());
            statement.setString(2, caps.getUrl());
            statement.setString(3, caps.getData());
            statement.execute();
        }
    }
}

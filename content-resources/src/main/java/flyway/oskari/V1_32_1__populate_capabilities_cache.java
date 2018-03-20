package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.ServiceException;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class V1_32_1__populate_capabilities_cache implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_32_1__populate_capabilities_cache.class);

    public void migrate(Connection connection) throws SQLException {
        final List<OskariLayer> layers = getLayers(connection);
        final int n = layers.size();

        LOG.info("Start populating capabilities for layers - count:", n);
        Set<String> keys = new HashSet<>();

        for (int i = 0; i < n; i++) {
            OskariLayer layer = layers.get(i);

            String url = layer.getSimplifiedUrl(true);
            String type = layer.getType();

            String layerKey = url + "----" + layer.getType();
            layerKey = layerKey.toLowerCase();
            if (keys.contains(layerKey)) {
                continue;
            }

            String data = "";
            try {
                data = CapabilitiesCacheService.getFromService(layer);
            } catch (ServiceException e) {
                LOG.error(e, "Error getting capabilities for service", url);
            }

            insertCaps(connection, type, url, data);

            // Don't try the same service again (another layer might have same url + type)
            keys.add(layerKey);

            LOG.info("Capabilities populated:", i + 1, "/", n);
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

    private void insertCaps(Connection conn, String type, String url, String data) throws SQLException {
        final String sql = "INSERT INTO oskari_capabilities_cache (layertype, url, data) VALUES(?,?,?)";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, type);
            statement.setString(2, url);
            statement.setString(3, data);
            statement.execute();
        }
    }
}

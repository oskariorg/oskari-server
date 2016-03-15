package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class V1_33_6__fix_and_repopulate_preparsed_layer_capabilities_for_layers extends V1_33_1_1__populate_preparsed_layer_capabilities {

    private static final Logger LOG = LogFactory.getLogger(V1_33_6__fix_and_repopulate_preparsed_layer_capabilities_for_layers.class);

    /**
     * Override capabilities fetch to use lowercased URLs this time
     * (URL is always saved with lowercase in the oskari_capabilities_cache).
     * @param url
     * @param conn
     * @return
     * @throws SQLException
     */
    String getCaps(String url, Connection conn)
            throws SQLException {
        // only process wms-layers
        final String sql = "SELECT data FROM oskari_capabilities_cache WHERE layertype='wmslayer' AND url=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, url.toLowerCase());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("data");
                }
                return null;
            }
        }
    }

    /**
     * Re-run migration on layers with empty capabilities (failed parsing in V1_33_1_1__populate_preparsed_layer_capabilities)
     * @param conn
     * @return
     * @throws SQLException
     */
    List<OskariLayer> getLayers(Connection conn)
            throws SQLException {
        List<OskariLayer> layers = new ArrayList<>();
        // only process wms-layers
        final String sql = "SELECT id, url, type, name FROM oskari_maplayer where type='wmslayer' and capabilities='{}'";
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

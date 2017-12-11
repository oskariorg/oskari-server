package flyway.oskari;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database helper for statsgrid migration
 */
public class ThematicMapsViewHelper {

    public static long getBundleId(Connection conn, String name) throws SQLException {
        String sql = "SELECT id FROM portti_bundle WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                return -1L;
            }
        }
    }

    public static List<ConfigNState> getConfigsAndStates(Connection conn, long bundleId) throws SQLException {
        String sql = "SELECT view_id, bundle_id, seqno, config, state"
                + " FROM portti_view_bundle_seq WHERE bundle_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bundleId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ConfigNState> configsAndStates = new ArrayList<>();
                while (rs.next()) {
                    ConfigNState cfg = new ConfigNState();
                    cfg.view_id = rs.getLong("view_id");
                    cfg.bundle_id = rs.getLong("bundle_id");
                    cfg.seqno = rs.getInt("seqno");
                    cfg.config = rs.getString("config");
                    cfg.state = rs.getString("state");
                    configsAndStates.add(cfg);
                }
                return configsAndStates;
            }
        }
    }

    public static void update(Connection conn, List<ConfigNState> configsAndStates) throws SQLException {
        String sql = "UPDATE portti_view_bundle_seq SET"
                + " config = ?, state = ?"
                + " WHERE view_id = ? AND bundle_id = ? AND seqno = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ConfigNState configAndState : configsAndStates) {
                ps.setString(1, configAndState.config);
                ps.setString(2, configAndState.state);
                ps.setLong(3, configAndState.view_id);
                ps.setLong(4, configAndState.bundle_id);
                ps.setInt(5, configAndState.seqno);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

}

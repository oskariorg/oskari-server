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

    public static ConfigNState getBundle(Connection conn, long id) throws SQLException {
        String sql = "SELECT id, config, state, startup FROM portti_bundle WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ConfigNState cfg = new ConfigNState();
                    cfg.bundle_id = rs.getLong("id");
                    cfg.config = rs.getString("config");
                    cfg.state = rs.getString("state");
                    cfg.startup = rs.getString("startup");
                    return cfg;
                }
                throw new SQLException("Couldnt find bundle with id " +id);
            }
        }
    }

    public static List<ConfigNState> getConfigsAndStates(Connection conn, long bundleId, String bundlePath) throws SQLException {
        String sql = "SELECT view_id, bundle_id, seqno, config, state, startup"
                + " FROM portti_view_bundle_seq WHERE bundle_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bundleId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ConfigNState> configsAndStates = new ArrayList<>();
                while (rs.next()) {
                    String startup = rs.getString("startup");
                    if(bundlePath != null && !startup.contains(bundlePath)) {
                        // we are only interested in bundles with old path
                        // not in ones using the new path
                        continue;
                    }
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

    public static void switchBundle(Connection conn, long old_bundle_id, long new_bundle_id) throws SQLException {
        final String startup = getBundle(conn, new_bundle_id).startup;
        String sql = "UPDATE portti_view_bundle_seq SET"
                + " startup = ?, bundle_id= ?, bundleinstance='statsgrid'"
                + " WHERE bundle_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startup);
            ps.setLong(2, new_bundle_id);
            ps.setLong(3, old_bundle_id);
            ps.execute();
        }
    }

    /**
     * Returns a list of view ids that contain a bundle (statsgrid), but don't have divmanazer (required by the new statsgrid)
     */
    public static List<Long> findAppsetupsHavingBundleButNoDivmanazer(Connection conn, long bundleId) throws SQLException {
        String sql = "SELECT view_id FROM portti_view_bundle_seq WHERE bundle_id = ? \n" +
                "and view_id not in (select view_id FROM portti_view_bundle_seq WHERE bundle_id = (select id from portti_bundle where name = 'divmanazer'))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bundleId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Long> idList = new ArrayList<>();
                while (rs.next()) {
                    idList.add(rs.getLong("view_id"));
                }
                return idList;
            }
        }
    }
    /**
     * Updates bundle sequence to make room for divmanazer after the mapfull bundle.
     * Uses private follow-up functions to do the update. This one only gets the bundles for a view in reversed order
     * (to make it easier the loop and update the seqno on the follow up method).
     */
    public static void injectDivmanazerAfterMapfull(Connection conn, long viewId, ConfigNState divmanazer, long mapfullId) throws SQLException {
        String sql = "SELECT view_id, bundle_id, seqno, config, state, startup, bundleinstance\n" +
                "  FROM portti_view_bundle_seq where view_id = ? order by seqno DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, viewId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ConfigNState> bundles = new ArrayList<>();
                while (rs.next()) {
                    ConfigNState cfg = new ConfigNState();
                    cfg.view_id = rs.getLong("view_id");
                    cfg.bundle_id = rs.getLong("bundle_id");
                    cfg.seqno = rs.getInt("seqno");
                    bundles.add(cfg);
                }
                // update seqno
                // NOTE! bundles are ordered last to first ("wrong" order)
                updateBundleSeq(conn, bundles, divmanazer, mapfullId);
            }
        }
    }

    /**
     * Updates bundle seqnos to make room for one bundle to be placed after the "mapfull" bundle.
     * Uses a follow-up method to inject the divmanazer bundle
     * @param conn
     * @param bundles
     * @param divmanazer
     * @param mapfullId
     * @throws SQLException
     */
    private static void updateBundleSeq(Connection conn, List<ConfigNState> bundles, ConfigNState divmanazer, long mapfullId) throws SQLException {
        String sql = "UPDATE portti_view_bundle_seq SET"
                + " seqno = ?"
                + " WHERE view_id = ? AND bundle_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int seqnoForDivManazer = -1;
            long viewId = -1;
            for (ConfigNState configAndState : bundles) {
                if(configAndState.bundle_id == mapfullId) {
                    viewId = configAndState.view_id;
                    break;
                }
                seqnoForDivManazer = configAndState.seqno;
                ps.setInt(1, configAndState.seqno + 1);
                ps.setLong(2, configAndState.view_id);
                ps.setLong(3, configAndState.bundle_id);
                ps.addBatch();
            }
            ps.executeBatch();
            if(seqnoForDivManazer == -1) {
                throw new RuntimeException("Couldn't find index to fit divmanazer in");
            }
            divmanazer.seqno = seqnoForDivManazer;
            insertDivmanazer(conn, divmanazer, seqnoForDivManazer, viewId);
        }
    }

    /**
     * Inserts divmanazer to a view with given sequence number
     */
    private static void insertDivmanazer(Connection conn, ConfigNState divmanazer, int seqno, long viewId) throws SQLException {

        final String sql ="INSERT INTO portti_view_bundle_seq" +
                "(view_id, bundle_id, seqno, config, state, startup, bundleinstance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(final PreparedStatement statement =
                    conn.prepareStatement(sql)) {
            statement.setLong(1, viewId);
            statement.setLong(2, divmanazer.bundle_id);
            statement.setInt(3, seqno);
            statement.setString(4, divmanazer.config);
            statement.setString(5, divmanazer.state);
            statement.setString(6, divmanazer.startup);
            statement.setString(7, "divmanazer");
            statement.execute();
        }
    }
}

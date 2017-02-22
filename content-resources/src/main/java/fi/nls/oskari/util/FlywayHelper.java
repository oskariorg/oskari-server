package fi.nls.oskari.util;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.ViewTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for flyway scripts. Be very careful when making changes as previous versions of Oskari are using this
 * to migrate database.
 */
public class FlywayHelper {

    public static List<Long> getViewIdsForTypes(Connection connection, String... types)
            throws SQLException {
        ArrayList<Long> ids = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id FROM portti_view");
        if (types != null && types.length > 0) {
            sql.append(" WHERE type IN (?");
            for (int i = 1; i < types.length; ++i) {
                sql.append(", ?");
            }
            sql.append(")");

        }
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {
            if (types != null) {
                for (int i = 0; i < types.length; ++i) {
                    statement.setString(i + 1, types[i]);
                }
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("id"));
                }
            }
        }
        return ids;
    }

    public static List<Long> getUserAndDefaultViewIds(Connection connection)
            throws SQLException {
        return getViewIdsForTypes(connection, ViewTypes.DEFAULT, ViewTypes.USER);
    }

    public static boolean viewContainsBundle(Connection connection, String bundle, Long viewId)
            throws SQLException {
        final String sql ="SELECT * FROM portti_view_bundle_seq " +
                "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name=?) " +
                "AND view_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle);
            statement.setLong(2, viewId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static Bundle getBundleFromView(Connection connection, String bundle, Long viewId)
            throws SQLException {
        final String sql ="SELECT * FROM portti_view_bundle_seq " +
                "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name=?) " +
                "AND view_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)){
            statement.setString(1, bundle);
            statement.setLong(2, viewId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Bundle b = new Bundle();
                    b.setViewId(viewId);
                    b.setName(bundle);
                    b.setBundleId(rs.getLong("bundle_id"));
                    b.setStartup(rs.getString("startup"));
                    b.setConfig(rs.getString("config"));
                    b.setState(rs.getString("state"));
                    b.setSeqNo(rs.getInt("seqno"));
                    b.setBundleinstance(rs.getString("bundleinstance"));
                    return b;
                }
            }
        }
        return null;
    }

    public static Bundle updateBundleInView(Connection connection, Bundle bundle, Long viewId)
            throws SQLException {
        final String sql = "UPDATE portti_view_bundle_seq SET " +
                "startup=?, " +
                "config=?, " +
                "state=?, " +
                "seqno=?, " +
                "bundleinstance=? " +
                " WHERE bundle_id=? " +
                " AND view_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.getStartup());
            statement.setString(2, bundle.getConfig());
            statement.setString(3, bundle.getState());
            statement.setInt(4, bundle.getSeqNo());
            statement.setString(5, bundle.getBundleinstance());
            statement.setLong(6, bundle.getBundleId());
            statement.setLong(7, viewId);
            statement.execute();
        }
        return null;
    }

    public static void addBundleWithDefaults(Connection connection, Long viewId, String bundleid)
            throws SQLException {
        final String sql ="INSERT INTO portti_view_bundle_seq" +
                "(view_id, bundle_id, seqno, config, state, startup, bundleinstance) " +
                "VALUES (" +
                "?, " +
                "(SELECT id FROM portti_bundle WHERE name=?), " +
                "(SELECT max(seqno)+1 FROM portti_view_bundle_seq WHERE view_id=?), " +
                "(SELECT config FROM portti_bundle WHERE name=?), " +
                "(SELECT state FROM portti_bundle WHERE name=?),  " +
                "(SELECT startup FROM portti_bundle WHERE name=?), " +
                "?)";
        try(final PreparedStatement statement =
                    connection.prepareStatement(sql)) {
            statement.setLong(1, viewId);
            statement.setString(2, bundleid);
            statement.setLong(3, viewId);
            statement.setString(4, bundleid);
            statement.setString(5, bundleid);
            statement.setString(6, bundleid);
            statement.setString(7, bundleid);
            statement.execute();
        }
    }
}

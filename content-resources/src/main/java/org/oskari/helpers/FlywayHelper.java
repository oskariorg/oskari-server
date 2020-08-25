package org.oskari.helpers;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<Long> getUserAndDefaultViewIds (Connection conn, String applicationName)
            throws SQLException {
        return getViewIdsForApplication(conn, applicationName, ViewTypes.DEFAULT, ViewTypes.USER);
    }

    public static List<Long> getViewIdsForApplication(Connection conn, String applicationName, String... types)
            throws SQLException {
        ArrayList<Long> ids = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id FROM portti_view WHERE application=?");
        if (types != null && types.length > 0) {
            sql.append(" AND type IN (?");
            for (int i = 1; i < types.length; ++i) {
                sql.append(", ?");
            }
            sql.append(")");

        }
        try (final PreparedStatement statement =
                     conn.prepareStatement(sql.toString())) {
            statement.setString(1, applicationName);
            if (types != null) {
                for (int i = 0; i < types.length; ++i) {
                    statement.setString(i + 2, types[i]);
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
    public static String getDefaultViewUuid(Connection conn, String applicationName) throws SQLException {
        Map<Long, String> uuids = new HashMap<>();
        final String sql = "SELECT id, uuid FROM portti_view WHERE application=? and type=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, applicationName);
            statement.setString(2, ViewTypes.DEFAULT);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    uuids.put(rs.getLong("id"), rs.getString("uuid"));
                }
            }
        }
        if (uuids.size() == 1) {
            return uuids.values().stream().findFirst().get();
        }
        if (uuids.size() > 1) {
            long defaultId = ConversionHelper.getLong(PropertyUtil.get("view.default"), -1);
            if (uuids.containsKey(defaultId)) {
                return uuids.get(defaultId);
            }
            long appId = ConversionHelper.getLong(PropertyUtil.get("view.default." + applicationName), -1);
            if (uuids.containsKey(appId)) {
                return uuids.get(appId);
            }
            throw new SQLException ("Couldn't find unique default view. Define default view id in properties view.default or view.default.{application}");
        }
        throw new SQLException ("Couldn't find default view");
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
                "config=?, " +
                "state=?, " +
                "seqno=?, " +
                "bundleinstance=? " +
                " WHERE bundle_id=? " +
                " AND view_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.getConfig());
            statement.setString(2, bundle.getState());
            statement.setInt(3, bundle.getSeqNo());
            statement.setString(4, bundle.getBundleinstance());
            statement.setLong(5, bundle.getBundleId());
            statement.setLong(6, viewId);
            statement.execute();
        }
        return null;
    }

    public static void addBundleWithDefaults(Connection connection, Long viewId, String bundleid)
            throws SQLException {
        final String sql ="INSERT INTO portti_view_bundle_seq" +
                "(view_id, bundle_id, seqno, config, state, bundleinstance) " +
                "VALUES (" +
                "?, " +
                "(SELECT id FROM portti_bundle WHERE name=?), " +
                "(SELECT max(seqno)+1 FROM portti_view_bundle_seq WHERE view_id=?), " +
                "(SELECT config FROM portti_bundle WHERE name=?), " +
                "(SELECT state FROM portti_bundle WHERE name=?),  " +
                "?)";
        try(final PreparedStatement statement =
                    connection.prepareStatement(sql)) {
            statement.setLong(1, viewId);
            statement.setString(2, bundleid);
            statement.setLong(3, viewId);
            statement.setString(4, bundleid);
            statement.setString(5, bundleid);
            statement.setString(6, bundleid);
            statement.execute();
        }
    }
    
    public static void removeBundleFromView(Connection connection, String bundleName, Long viewId)
            throws SQLException {
        final String sql ="DELETE FROM portti_view_bundle_seq " +
                "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name=?) AND view_id=?";
        try(final PreparedStatement statement =
                    connection.prepareStatement(sql)) {
            statement.setString(1, bundleName);
            statement.setLong(2, viewId);
            statement.execute();
        }
    }
}

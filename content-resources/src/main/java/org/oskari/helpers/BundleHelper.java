package org.oskari.helpers;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceMybatisImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BundleHelper {

    private static final Logger LOG = LogFactory.getLogger(BundleHelper.class);
    private static final BundleService SERVICE = new BundleServiceMybatisImpl();

    private BundleHelper() {
    }

    public static boolean isBundleRegistered(final String id) {
        return SERVICE.getBundleTemplateByName(id) != null;
    }

    public static void registerBundle(final Bundle bundle) {
        if (isBundleRegistered(bundle.getName())) {
            // already registered
            LOG.info("Bundle", bundle.getName(), "already registered - Skipping!");
            return;
        }
        SERVICE.addBundleTemplate(bundle);
    }

    public static boolean isBundleRegistered(Connection conn, final String name) throws SQLException {
        return getRegisteredBundle(conn, name) != null;
    }

    public static void registerBundle(Connection conn, String name) throws SQLException {
        registerBundle(conn, new Bundle(name));
    }

    public static void registerBundle(Connection conn, Bundle bundle) throws SQLException {
        if (isBundleRegistered(conn, bundle.getName())) {
            // already registered
            LOG.info("Bundle", bundle.getName(), "already registered - Skipping!");
            return;
        }

        try (PreparedStatement statement = conn
                .prepareStatement("INSERT INTO oskari_bundle(name, config, state) VALUES(?,?,?)")) {
            statement.setString(1, bundle.getName());
            statement.setString(2, bundle.getConfig());
            statement.setString(3, bundle.getState());
            statement.execute();
        }
    }

    public static Bundle getRegisteredBundle(Connection conn, String name) throws SQLException {
        try (PreparedStatement statement = conn
                .prepareStatement("SELECT id, name, config, state FROM oskari_bundle WHERE name=?")) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Bundle b = new Bundle();
                b.setBundleId(rs.getLong("id"));
                b.setName(rs.getString("name"));
                b.setConfig(rs.getString("config"));
                b.setState(rs.getString("state"));
                return b;
            }
        }
    }

    public static void unregisterBundle(Connection conn, String bundleName) throws SQLException {
        if (isBundleRegistered(conn, bundleName)) {
            try (PreparedStatement statement = conn.prepareStatement("DELETE FROM oskari_bundle WHERE name=?")) {
                statement.setString(1, bundleName);
                statement.execute();
            }
        } else {
            LOG.info("Bundle", bundleName, "not registered - Skipping!");
        }
    }
}

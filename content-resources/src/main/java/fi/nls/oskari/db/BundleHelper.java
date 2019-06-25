package fi.nls.oskari.db;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceMybatisImpl;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.OskariRuntimeException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BundleHelper {

    private static final Logger LOG = LogFactory.getLogger(BundleHelper.class);
    private static final BundleService SERVICE = new BundleServiceMybatisImpl();

    private BundleHelper() {
    }

    /**
     * @deprecated startup is no longer used for bundles
     * @param namespace
     * @param bundleid
     * @param title
     * @return
     */
    public static String getDefaultBundleStartup(final String namespace, final String bundleid, final String title) {
        return null;
    }

    public static String getBundleStartup(final String path, final String bundleid, final String title) {
        if (bundleid == null) {
            throw new OskariRuntimeException("Missing bundleid");
        }
        return null;
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

    public static boolean isBundleRegistered(final String id, Connection conn) throws SQLException {
        return getRegisteredBundle(id, conn) != null;
    }

    public static void registerBundle(final Bundle bundle, Connection conn) throws SQLException {
        if (isBundleRegistered(bundle.getName(), conn)) {
            // already registered
            LOG.info("Bundle", bundle.getName(), "already registered - Skipping!");
            return;
        }

        try (PreparedStatement statement = conn
                .prepareStatement("INSERT INTO portti_bundle(name, config, state) VALUES(?,?,?)")) {
            statement.setString(1, bundle.getName());
            statement.setString(2, bundle.getConfig());
            statement.setString(3, bundle.getState());
            statement.execute();
        }
    }

    public static Bundle getRegisteredBundle(final String id, Connection conn) throws SQLException {
        try (PreparedStatement statement = conn
                .prepareStatement("SELECT id, name, config, state FROM portti_bundle WHERE name=?")) {
            statement.setString(1, id);
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

    public static void unregisterBundle(final String bundleName, Connection conn) throws SQLException {
        if (isBundleRegistered(bundleName, conn)) {
            try (PreparedStatement statement = conn.prepareStatement("DELETE FROM portti_bundle WHERE name=?")) {
                statement.setString(1, bundleName);
                statement.execute();
            }
        } else {
            LOG.info("Bundle", bundleName, "not registered - Skipping!");
        }
    }
}

package fi.nls.oskari.db;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.OskariRuntimeException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 27.6.2014
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class BundleHelper {

    private static final Logger LOG = LogFactory.getLogger(BundleHelper.class);
    private static final BundleService SERVICE = new BundleServiceIbatisImpl();

    private static final String BUNDLE_STARTUP_TEMPLATE = "BundleHelper-startup.json";
    private static String startupTemplate = "";
    static {
        try {
            startupTemplate = IOHelper.readString(BundleHelper.class.getResourceAsStream(BUNDLE_STARTUP_TEMPLATE));
        } catch (IOException ex) {
            throw new OskariRuntimeException("Error reading startup template", ex);
        }
    }

    private BundleHelper() {
    }

    public static String getDefaultBundleStartup(final String namespace, final String bundleid, final String title) {
        final String ns = namespace == null ? "framework" : namespace;
        final String path = "/Oskari/packages/%s/bundle/";
        return getBundleStartup(String.format(path, ns), bundleid, title);
    }
    public static String getBundleStartup(final String path, final String bundleid, final String title) {
        if(bundleid == null) {
            throw new OskariRuntimeException("Missing bundleid");
        }
        final String label = title == null ? bundleid : title;
        return String.format(startupTemplate, label, bundleid, bundleid, bundleid, path);
    }

    public static boolean isBundleRegistered(final String id) {
        return SERVICE.getBundleTemplateByName(id) != null;
    }

    public static void registerBundle(final Bundle bundle) {
        if(isBundleRegistered(bundle.getName())) {
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
        if(isBundleRegistered(bundle.getName(), conn)) {
            // already registered
            LOG.info("Bundle", bundle.getName(), "already registered - Skipping!");
            return;
        }

        try(PreparedStatement statement =
                    conn.prepareStatement("INSERT INTO portti_bundle(name, startup, config, state) VALUES(?,?,?,?)")) {
            statement.setString(1,bundle.getName());
            statement.setString(2,bundle.getStartup());
            statement.setString(3,bundle.getConfig());
            statement.setString(4,bundle.getState());
            statement.execute();
        }
    }

    public static Bundle getRegisteredBundle(final String id, Connection conn) throws SQLException {
        try(PreparedStatement statement =
                    conn.prepareStatement("SELECT id, name, startup, config, state FROM portti_bundle WHERE name=?")) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if(!rs.next()) {
                    return null;
                }
                Bundle b = new Bundle();
                b.setBundleId(rs.getLong("id"));
                b.setName(rs.getString("name"));
                b.setStartup(rs.getString("startup"));
                b.setConfig(rs.getString("config"));
                b.setState(rs.getString("state"));
                return b;
            }
        }
    }
}

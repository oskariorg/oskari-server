package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

/**
 * Removes deprecated packages from mapfull bundle imports
 */
public class V1_41_6__remove_empty_packages implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_41_6__remove_empty_packages.class);

    private static final Set<String> TO_BE_REMOVED = ConversionHelper.asSet("request-base", "request-map",
                    "request-map-layer", "event-base", "event-map", "event-map-layer",
                    "core-base", "core-map", "service-base", "service-map",
                    "domain", "sandbox-map", "sandbox-base");

    public void migrate(Connection connection)
            throws Exception {
        Bundle mapfull = getMapFullTemplate(connection);
        if(modifyStartup(mapfull)) {
            // update template back to db
            updateBundleTemplate(connection, mapfull);
        }

        final ArrayList<Bundle> mapfullBundles = getMapFullBundles(connection);
        for(Bundle bundle: mapfullBundles) {
            if(!modifyStartup(bundle)) {
                continue;
            }
            // update view back to db
            updateBundleInView(connection, bundle);
        }

    }
    private boolean modifyStartup(Bundle bundle) throws Exception {
        JSONObject startup = JSONHelper.createJSONObject(bundle.startup);
        if(startup == null) {
            LOG.warn("Couldn't get startup JSON for view:", bundle.viewId);
            return false;
        }
        JSONObject metadata = startup.optJSONObject("metadata");
        if(metadata == null) {
            LOG.warn("Couldn't get startup.metadata JSON for view:", bundle.viewId);
            return false;
        }
        JSONObject imports = metadata.optJSONObject("Import-Bundle");
        if(imports == null) {
            LOG.warn("Couldn't get startup.metadata.Import-Bundle JSON for view:", bundle.viewId);
            return false;
        }
        for(String deprecated : TO_BE_REMOVED) {
            imports.remove(deprecated);
        }
        bundle.startup = startup.toString(2);
        return true;
    }


    private ArrayList<Bundle> getMapFullBundles(Connection connection) throws Exception {
        ArrayList<Bundle> ids = new ArrayList<>();
        final String sql = "SELECT view_id, bundle_id, startup FROM portti_view_bundle_seq " +
                "WHERE bundle_id = (select id from portti_bundle where name = 'mapfull')";
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                Bundle b = new Bundle();
                b.viewId = rs.getLong("view_id");
                b.bundleId = rs.getLong("bundle_id");
                b.startup = rs.getString("startup");
                ids.add(b);
            }
        }
        return ids;
    }

    public static Bundle updateBundleInView(Connection connection, Bundle bundle)
            throws SQLException {
        final String sql = "UPDATE portti_view_bundle_seq SET " +
                "startup=? " +
                " WHERE bundle_id=? " +
                " AND view_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.startup);
            statement.setLong(2, bundle.bundleId);
            statement.setLong(3, bundle.viewId);
            statement.execute();
        }
        return null;
    }

    private Bundle getMapFullTemplate(Connection connection) throws Exception {
        final String sql = "SELECT id, startup FROM portti_bundle WHERE name = 'mapfull'";
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                Bundle b = new Bundle();
                b.bundleId = rs.getLong("id");
                b.startup = rs.getString("startup");
                return b;
            }
        }
        return null;
    }

    public static Bundle updateBundleTemplate(Connection connection, Bundle bundle)
            throws SQLException {
        final String sql = "UPDATE portti_bundle SET " +
                "startup=? " +
                " WHERE id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.startup);
            statement.setLong(2, bundle.bundleId);
            statement.execute();
        }
        return null;
    }

    class Bundle {
        long viewId;
        long bundleId;
        String startup;
    }
}

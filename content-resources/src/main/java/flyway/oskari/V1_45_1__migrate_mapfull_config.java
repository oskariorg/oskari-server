package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Removes "globalMapAjaxUrl" and "user" keys from mapfull bundle configs in portti_bundle and portti_view_bundle_seq
 * as they are now part of the "global environment variables" instead of mapfull config
 */
public class V1_45_1__migrate_mapfull_config implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_45_1__migrate_mapfull_config.class);

    public void migrate(Connection connection)
            throws Exception {
        Bundle mapfull = getMapFullTemplate(connection);
        if(modifyConfig(mapfull)) {
            // update template back to db
            updateBundleTemplate(connection, mapfull);
        }

        final ArrayList<Bundle> mapfullBundles = getMapFullBundles(connection);
        for(Bundle bundle: mapfullBundles) {
            if(!modifyConfig(bundle)) {
                continue;
            }
            // update view back to db
            updateBundleInView(connection, bundle);
        }

    }
    private boolean modifyConfig(Bundle bundle) throws Exception {
        JSONObject config = JSONHelper.createJSONObject(bundle.config);
        if(config == null) {
            LOG.warn("Couldn't get config JSON for view:", bundle.viewId);
            return false;
        }
        config.remove("globalMapAjaxUrl");
        config.remove("user");
        bundle.config = config.toString(2);
        return true;
    }


    private ArrayList<Bundle> getMapFullBundles(Connection connection) throws Exception {
        ArrayList<Bundle> ids = new ArrayList<>();
        final String sql = "SELECT view_id, bundle_id, config FROM portti_view_bundle_seq " +
                "WHERE bundle_id = (select id from portti_bundle where name = 'mapfull')";
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                Bundle b = new Bundle();
                b.viewId = rs.getLong("view_id");
                b.bundleId = rs.getLong("bundle_id");
                b.config = rs.getString("config");
                ids.add(b);
            }
        }
        return ids;
    }

    public static Bundle updateBundleInView(Connection connection, Bundle bundle)
            throws SQLException {
        final String sql = "UPDATE portti_view_bundle_seq SET " +
                "config=? " +
                " WHERE bundle_id=? " +
                " AND view_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.config);
            statement.setLong(2, bundle.bundleId);
            statement.setLong(3, bundle.viewId);
            statement.execute();
        }
        return null;
    }

    private Bundle getMapFullTemplate(Connection connection) throws Exception {
        final String sql = "SELECT id, config FROM portti_bundle WHERE name = 'mapfull'";
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                Bundle b = new Bundle();
                b.bundleId = rs.getLong("id");
                b.config = rs.getString("config");
                return b;
            }
        }
        return null;
    }

    public static Bundle updateBundleTemplate(Connection connection, Bundle bundle)
            throws SQLException {
        final String sql = "UPDATE portti_bundle SET " +
                "config=? " +
                " WHERE id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.config);
            statement.setLong(2, bundle.bundleId);
            statement.execute();
        }
        return null;
    }

    class Bundle {
        long viewId;
        long bundleId;
        String config;
    }
}

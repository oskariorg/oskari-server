package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Replaces transport based wfs plugin with vector implementation
 *
 * The version number doesn't match Oskari version like it should (should be V1_53_x) but it's hard to change the earlier
 * migration that used 1.54 already so this just needs a higher version number...
 */
public class V1_54_1__migrate_wfs_system implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_54_1__migrate_wfs_system.class);
    private static final String WFS_TRANSPORT_PLUGIN_ID = "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin";
    private static final String WFS_VECTOR_PLUGIN_ID = "Oskari.wfsvector.WfsVectorLayerPlugin";
    private static final String PLUGIN_CONFIG = "config";
    private static final String MIGRATION_PROP_NAME = "flyway.1.53.wfs.optout";

    public void migrate(Connection connection) throws Exception {
        final boolean optout = PropertyUtil.getOptional(MIGRATION_PROP_NAME, false);
        if (optout) {
            LOG.warn("Skipping migration to new wfs system. This will be forced on the next version.");
            return;
        }
        List<Bundle> mapfullBundles = getMapfullBundles(connection);
        for (Bundle mapfull : mapfullBundles) {
            modifyConfig(mapfull);
            updateBundle(mapfull, connection);
        }
        connection.commit();
    }

    private List<Bundle> getMapfullBundles(Connection conn) throws SQLException {
        List<Bundle> list = new ArrayList<>();
        String sql = "select view_id, bundle_id, config from portti_view_bundle_seq \n" +
                "where bundle_id = (select id from portti_bundle where name = 'mapfull')";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    Bundle bundle = new Bundle();
                    bundle.id = rs.getLong("bundle_id");
                    bundle.view = rs.getLong("view_id");
                    bundle.config = JSONHelper.createJSONObject(rs.getString("config"));
                    if(bundle.config == null) {
                        bundle.config = new JSONObject();
                    }
                    list.add(bundle);
                }
            }
        }
        return list;
    }

    private void modifyConfig (Bundle mapfull) throws Exception {
        if (mapfull == null) {
            return;
        }
        JSONArray plugins = mapfull.config.optJSONArray("plugins");
        if (plugins == null) {
            return;
        }
        for (int i = 0; i < plugins.length(); i++) {
            JSONObject plugin = plugins.getJSONObject(i);
            String id = plugin.optString("id");
            if (id == null || !id.equals(WFS_TRANSPORT_PLUGIN_ID)) {
                continue;
            }
            if (plugin.has(PLUGIN_CONFIG)) {
                plugin.remove(PLUGIN_CONFIG);
            }
            plugin.put("id", WFS_VECTOR_PLUGIN_ID);
        }
    }

    private void updateBundle(Bundle bundle, Connection conn) throws SQLException {
        final String sql = "UPDATE portti_view_bundle_seq SET config=?, where view_id=? AND bundle_id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, bundle.config.toString());
            statement.setLong(2, bundle.view);
            statement.setLong(3, bundle.id);
            statement.execute();
        }
    }

    class Bundle {
        long view;
        long id;
        JSONObject config;
    }
}

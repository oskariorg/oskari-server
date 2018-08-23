package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.apache.ibatis.jdbc.SQL;
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
 * Migrates all mapfull configs and startups in the database by removing references to mapstats and
 * 'Oskari.mapframework.bundle.mapstats.plugin.StatsLayerPlugin'
 */
public class V1_48_1__remove_statslayerplugin implements JdbcMigration {

    private static final Logger LOG  = LogFactory.getLogger(V1_48_1__remove_statslayerplugin.class);

    class Bundle {
        long view;
        long id;
        JSONObject startup;
        JSONObject config;
    }

    public void migrate(Connection connection) throws SQLException {
        // appsetups
        List<Bundle> bundles = getMapfullBundles(connection);
        for(Bundle mapfull : bundles) {
            boolean removedPlugin = removeStatslayerPlugin(mapfull.config);
            boolean removedImport = removeMapstatsImport(mapfull.startup);
            if(removedImport || removedPlugin) {
                updateBundle(mapfull, connection);
            }
        }

        // template
        Bundle template = getMapfullBundleTemplate(connection);
        boolean removedPlugin = removeStatslayerPlugin(template.config);
        boolean removedImport = removeMapstatsImport(template.startup);
        if(removedImport || removedPlugin) {
            updateBundleTemplate(template, connection);
        }
        connection.commit();
    }

    private boolean removeStatslayerPlugin(JSONObject mapfullConfig) {
        JSONArray plugins = mapfullConfig.optJSONArray("plugins");
        JSONArray newPlugins = new JSONArray();
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.optJSONObject(i);
            if("Oskari.mapframework.bundle.mapstats.plugin.StatsLayerPlugin".equals(plugin.optString("id"))) {
                // this is the plugin to remove
                continue;
            }
            newPlugins.put(plugin);
        }
        JSONHelper.putValue(mapfullConfig, "plugins", newPlugins);
        return plugins.length() != newPlugins.length();
    }

    private boolean removeMapstatsImport(JSONObject startup) {
        if (startup == null) {
            return false;
        }
        JSONObject metadata = startup.optJSONObject("metadata");

        if (metadata == null) {
            return false;
        }
        JSONObject bundles = metadata.optJSONObject("Import-Bundle");
        if (bundles == null) {
            return false;
        }
        return bundles.remove("mapstats") != null;
    }

    private List<Bundle> getMapfullBundles(Connection conn) throws SQLException {
        List<Bundle> list = new ArrayList<>();
        String sql = "select view_id, bundle_id, config, startup from portti_view_bundle_seq \n" +
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
                    bundle.startup = JSONHelper.createJSONObject(rs.getString("startup"));
                    if(bundle.startup == null) {
                        bundle.startup = new JSONObject();
                    }
                    list.add(bundle);
                }
            }
        }
        return list;
    }

    private Bundle getMapfullBundleTemplate(Connection conn) throws SQLException {
        String sql = "select id, config, startup from portti_bundle where name = 'mapfull'";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                Bundle bundle = new Bundle();
                bundle.id = rs.getLong("id");
                bundle.config = JSONHelper.createJSONObject(rs.getString("config"));
                if(bundle.config == null) {
                    bundle.config = new JSONObject();
                }
                bundle.startup = JSONHelper.createJSONObject(rs.getString("startup"));
                if(bundle.startup == null) {
                    bundle.startup = new JSONObject();
                }
                return bundle;
            }
        }
    }

    private void updateBundle(Bundle bundle, Connection conn) throws SQLException {
        final String sql = "UPDATE portti_view_bundle_seq SET config=?, startup=? where view_id=? AND bundle_id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, bundle.config.toString());
            statement.setString(2, bundle.startup.toString());
            statement.setLong(3, bundle.view);
            statement.setLong(4, bundle.id);
            statement.execute();
        }
    }

    private void updateBundleTemplate(Bundle bundle, Connection conn) throws SQLException {
        final String sql = "UPDATE portti_bundle SET config=?, startup=? where id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, bundle.config.toString());
            statement.setString(2, bundle.startup.toString());
            statement.setLong(3, bundle.id);
            statement.execute();
        }
    }

}

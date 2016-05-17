package flyway.oskari;

import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class V1_36_7__migrate_published_views_toolbar implements JdbcMigration {

    class Bundle {
        long view;
        long id;
        JSONObject config;
    }

    public void migrate(Connection connection) throws SQLException {
        // find all published maps with toolbar bundle
        List<Bundle> toolbars = getToolbarBundlesInPublishedMaps(connection);
        for(Bundle toolbar : toolbars) {
            // get mapfull bundle for the view
            Bundle mapfull = getMapfullBundle(toolbar.view, connection);
            // find if there is the toolbar plugin available in mapfull
            JSONObject pluginConfig = getToolbarPluginConfig(mapfull.config);
            // if there is, lets update its config
            if(pluginConfig != null) {
                // get list of toolbar tools configured for published map
                JSONArray pluginButtonsList = getButtons(toolbar.config);
                if(pluginButtonsList.length() > 0) {
                    // if there were buttons, update the toolbar plugin config in mapfull config
                    JSONHelper.putValue(pluginConfig, "buttons", pluginButtonsList);
                    // update mapfull in database
                    updateBundleConfig(mapfull, connection);
                }
            }
            // reset toolbar config so all default buttons are off
            toolbar.config = getNewToolbarConfig();

            // update toolbar in database
            updateBundleConfig(toolbar, connection);
        }
    }

    private JSONObject getNewToolbarConfig() {
        return JSONHelper.createJSONObject("{\"history\": false,\"basictools\": false,\"viewtools\": false }");
    }

    private JSONObject getToolbarPluginConfig(JSONObject mapfullConfig) {
        JSONArray plugins = mapfullConfig.optJSONArray("plugins");
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.optJSONObject(i);
            if(!"Oskari.mapframework.bundle.mapmodule.plugin.PublisherToolbarPlugin".equals(plugin.optString("id"))) {
                // this is not the plugin you are looking for
                continue;
            }
            JSONObject config = plugin.optJSONObject("config");
            if(config == null) {
                config = new JSONObject();
                JSONHelper.putValue(plugin, "config", config);
            }
            return config;
        }
        return null;
    }

    private JSONArray getButtons(JSONObject config) {
        JSONArray list = new JSONArray();
        JSONObject history = config.optJSONObject("history");
        if(history != null) {
            if(history.optBoolean("history_back")) {
                list.put("history_back");
            }
            if(history.optBoolean("history_forward")) {
                list.put("history_forward");
            }
        }
        JSONObject basictools = config.optJSONObject("basictools");
        if(basictools != null) {
            if(basictools.optBoolean("measureline")) {
                list.put("measureline");
            }
            if(basictools.optBoolean("measurearea")) {
                list.put("measurearea");
            }
        }
        return list;
    }

    private List<Bundle> getToolbarBundlesInPublishedMaps(Connection conn) throws SQLException {
        List<Bundle> list = new ArrayList<>();
        String sql = "select view_id, bundle_id, config from portti_view_bundle_seq \n" +
                "where view_id in (SELECT id FROM portti_view where type = 'PUBLISHED') \n" +
                "and bundle_id = (select id from portti_bundle where name = 'toolbar') \n" +
                "and config <> '{}'";
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
    private Bundle getMapfullBundle(long viewId, Connection conn) throws SQLException {
        String sql = "select view_id, bundle_id, config from portti_view_bundle_seq \n" +
                "where view_id =? \n" +
                "and bundle_id = (select id from portti_bundle where name = 'mapfull')";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, viewId);
            try (ResultSet rs = statement.executeQuery()) {
                if(rs.next()) {
                    Bundle bundle = new Bundle();
                    bundle.id = rs.getLong("bundle_id");
                    bundle.view = rs.getLong("view_id");
                    bundle.config = JSONHelper.createJSONObject(rs.getString("config"));
                    if(bundle.config == null) {
                        bundle.config = new JSONObject();
                    }
                    return bundle;
                }
            }
        }
        return null;
    }

    private void updateBundleConfig(Bundle bundle, Connection conn) throws SQLException {
        final String sql = "UPDATE portti_view_bundle_seq SET config=? where view_id=? AND bundle_id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, bundle.config.toString());
            statement.setLong(2, bundle.view);
            statement.setLong(3, bundle.id);
            statement.execute();
        }
    }
}

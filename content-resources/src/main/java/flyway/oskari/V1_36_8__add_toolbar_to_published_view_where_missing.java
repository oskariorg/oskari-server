package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
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

/*
-- published views with toolbar
select count(view_id) from portti_view_bundle_seq
 where view_id in (SELECT id FROM portti_view where type = 'PUBLISHED')
and bundle_id = (select id from portti_bundle where name = 'toolbar')

-- published views without toolbar
select count(id) from portti_view where type = 'PUBLISHED' and id NOT IN (select view_id from portti_view_bundle_seq
 where view_id in (SELECT id FROM portti_view where type = 'PUBLISHED')
and bundle_id = (select id from portti_bundle where name = 'toolbar'))
 */
public class V1_36_8__add_toolbar_to_published_view_where_missing implements JdbcMigration {

    private static final Logger LOG  = LogFactory.getLogger(V1_36_8__add_toolbar_to_published_view_where_missing.class);

    private static final String TOOLBAR_TEMPLATE = "{\n" +
            "        \"bundlename\" : \"toolbar\",\n" +
            "        \"metadata\" : {\n" +
            "            \"Import-Bundle\" : {\n" +
            "                \"toolbar\": {\n" +
            "                    \"bundlePath\": \"%s\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }";

    class Bundle {
        long view;
        long id;
        JSONObject startup;
        JSONObject config;
    }

    public void migrate(Connection connection) throws Exception {
        // find all published maps without toolbar bundle
        List<Long> viewsNotUpdated = new ArrayList<>();
        List<Long> viewsToUpdate = getPublishedViewsWithoutToolbarBundle(connection);
        long toolbarId = getIdForToolbarBundle(connection);
        for(long id : viewsToUpdate) {
            Bundle mapfull = getMapfullBundle(id, connection);
            try {
                boolean useOl3 = isOl3(mapfull);
                Bundle toolbar = getToolbar(useOl3);
                toolbar.id = toolbarId;
                toolbar.view = id;
                // insert toolbar to view with id
                fixMarkersPluginConfig(mapfull.config);
                updateBundleConfig(mapfull, connection);
                insertBundle(toolbar, connection);

            } catch (Exception ex) {
                LOG.error("Error updating view with id", id, ": ", ex.getMessage());
                viewsNotUpdated.add(id);
            }
        }
        if(viewsToUpdate.size() > 0 && viewsNotUpdated.size() == viewsToUpdate.size()) {
            throw new Exception("Views to update, but didn't update any. Tried " + viewsToUpdate.size());
        }
        if(viewsNotUpdated.size() > 0) {
            LOG.warn("Error updating toolbar to", viewsNotUpdated.size(), "views:", viewsNotUpdated);
        }
    }

    private boolean isOl3(Bundle mapfull) throws Exception {
        JSONObject metadata = mapfull.startup.optJSONObject("metadata");
        JSONObject imports = metadata.optJSONObject("Import-Bundle");
        JSONObject mapmodule = imports.optJSONObject("mapmodule");
        if(mapmodule != null) {
            // propably ol3
            String path = mapmodule.optString("bundlePath");
            if(!path.contains("packages/mapping/ol3")) {
                throw new Exception("Didn't detect either ol3 or ol2 mapmodule - cancel update");
            }
            // definately ol3
            return true;
        }
        // ol2 version is mapmodule-plugin from packages/framework/bundle/
        JSONObject mapmodulePlugin = imports.optJSONObject("mapmodule-plugin");
        if(mapmodulePlugin == null) {
            throw new Exception("Didn't detect either ol3 or ol2 mapmodule - cancel update");
        }
        String path = mapmodulePlugin.optString("bundlePath");
        // propably ol2 version
        if(!path.contains("packages/framework/bundle")) {
            throw new Exception("Didn't detect either ol3 or ol2 mapmodule - cancel update");
        }
        // was ol2
        return false;
    }

    private Bundle getToolbar(boolean isOL3) {
        Bundle toolbar = new Bundle();
        toolbar.config = JSONHelper.createJSONObject("{\"history\": false,\"basictools\": false,\"viewtools\": false }");
        // OL3
        String path = "/Oskari/packages/mapping/ol3/";
        if(!isOL3) {
            // OL2
            path = "/Oskari/packages/framework/bundle/";
        }
        String startup = String.format(TOOLBAR_TEMPLATE, path);
        toolbar.startup = JSONHelper.createJSONObject(startup);
        return toolbar;
    }

    private void fixMarkersPluginConfig(JSONObject mapfullConfig) {
        JSONArray plugins = mapfullConfig.optJSONArray("plugins");
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.optJSONObject(i);
            if(!"Oskari.mapframework.mapmodule.MarkersPlugin".equals(plugin.optString("id"))) {
                // this is not the plugin you are looking for
                continue;
            }
            JSONObject config = plugin.optJSONObject("config");
            if(config == null) {
                config = new JSONObject();
                JSONHelper.putValue(plugin, "config", config);
            }
            // Attach markerButton false to any marker plugin!!
            // Otherwise the plugin will add a button to toolbar
            // config = { "markerButton" : false}
            JSONHelper.putValue(config, "markerButton", false);
            break;
        }
    }

    private List<Long> getPublishedViewsWithoutToolbarBundle(Connection conn) throws SQLException {
        List<Long> list = new ArrayList<>();
        String sql = "select id from portti_view where type = 'PUBLISHED' and id NOT IN (select view_id from portti_view_bundle_seq\n" +
                " where view_id in (SELECT id FROM portti_view where type = 'PUBLISHED')\n" +
                "and bundle_id = (select id from portti_bundle where name = 'toolbar'))";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    list.add(rs.getLong("id"));
                }
            }
        }
        return list;
    }


    private Long getIdForToolbarBundle(Connection conn) throws Exception {
        String sql = "select id from portti_bundle where name = 'toolbar'";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                if(rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        throw new Exception("Toolbar bundle not in portti_bundle");
    }


    private Bundle getMapfullBundle(long viewId, Connection conn) throws SQLException {
        String sql = "select view_id, bundle_id, config, startup from portti_view_bundle_seq \n" +
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
                    bundle.startup = JSONHelper.createJSONObject(rs.getString("startup"));
                    if(bundle.startup == null) {
                        bundle.startup = new JSONObject();
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

    private void insertBundle(Bundle bundle, Connection conn) throws SQLException {
        final String sql = "INSERT INTO portti_view_bundle_seq (view_id, bundle_id, config, startup, seqno) VALUES (?, ?, ?, ?, (SELECT (max(seqno) + 1) FROM portti_view_bundle_seq WHERE view_id = ?))";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, bundle.view);
            statement.setLong(2, bundle.id);
            statement.setString(3, bundle.config.toString());
            statement.setString(4, bundle.startup.toString());
            statement.setLong(5, bundle.view);
            statement.execute();
        }
    }

}

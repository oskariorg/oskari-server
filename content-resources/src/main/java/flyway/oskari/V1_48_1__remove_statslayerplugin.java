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
public class V1_48_1__remove_statslayerplugin implements JdbcMigration {

    private static final Logger LOG  = LogFactory.getLogger(V1_48_1__remove_statslayerplugin.class);

    class Bundle {
        long view;
        long id;
        JSONObject startup;
        JSONObject config;
    }

    public void migrate(Connection connection) throws SQLException {
        if(true) throw new SQLException("Not impl yet");
        List<Bundle> bundles = getMapfullBundles(connection);
        for(Bundle mapfull : bundles) {
            boolean removedPlugin = removeStatslayerPlugin(mapfull.config);
            //removeMapstatsImport(mapfull.startup);
            //updateBundleConfig(mapfull, connection);
        }
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

    private List<Bundle> getMapfullBundles(Connection conn) throws SQLException {
        List<Bundle> list = new ArrayList<>();
        String sql = "select view_id, bundle_id, config, startup from portti_view_bundle_seq \n" +
                "where bundle_id = (select id from portti_bundle where name = 'mapfull')";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
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
                    list.add(bundle);
                }
            }
        }
        return list;
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

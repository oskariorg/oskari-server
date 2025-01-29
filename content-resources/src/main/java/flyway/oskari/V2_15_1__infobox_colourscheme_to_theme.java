package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class V2_15_1__infobox_colourscheme_to_theme  extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V2_15_1__infobox_colourscheme_to_theme.class);
    private static final String INFO_PLUGIN_CLASS = "Oskari.mapframework.mapmodule.GetInfoPlugin";
    @Override
    public void migrate (Context context) throws Exception {
        Connection conn = context.getConnection();
        JSONArray mapModules = getMapfullConfigsWithInfobox(conn);

        JSONArray readyForUpdate = updateColourSchemeToTheme(mapModules);
        updateTheme(conn, readyForUpdate);
        removeColourScheme(conn, readyForUpdate);
    }

    private void updateTheme(Connection connection, JSONArray readyForUpdate) throws SQLException, JSONException {
        final String sql = "UPDATE oskari_appsetup SET metadata = ? WHERE  id = ?";
        for (int i = 0; i < readyForUpdate.length(); i++) {
            JSONObject module = readyForUpdate.getJSONObject(i);
            JSONObject metadata = new JSONObject(module.getString("metadata"));
            int appsetupId = module.getInt("appsetup_id");
            try (final PreparedStatement statement =
                         connection.prepareStatement(sql)) {
                statement.setString(1, metadata.toString());
                statement.setLong(2, appsetupId);
                // LOG.info(i+" - Updating view " + appsetupId + " with theme " + metadata.getString("theme"));
                statement.execute();
            }
        }
    }

    private void removeColourScheme(Connection connection, JSONArray readyForUpdate) throws SQLException, JSONException {
        final String sql = "UPDATE oskari_appsetup_bundles SET config = ? WHERE  appsetup_id = ? AND bundle_id = (select id from oskari_bundle where  name = 'mapfull')";
        for (int i = 0; i < readyForUpdate.length(); i++) {
            JSONObject module = readyForUpdate.getJSONObject(i);
            JSONObject config = new JSONObject(module.getString("config"));
            int appsetupId = module.getInt("appsetup_id");
            try (final PreparedStatement statement =
                         connection.prepareStatement(sql)) {
                statement.setString(1, config.toString());
                statement.setLong(2, appsetupId);
                // LOG.info(i+" - Updating view " + appsetupId + " with config " + config.toString());
                statement.execute();
            }
        }
    }
    private JSONArray getMapfullConfigsWithInfobox(Connection conn)  throws SQLException {
        final String sql = "SELECT setup_bundles.appsetup_id, setup_bundles.bundle_id, setup_bundles.config, setup.metadata " +
            "FROM oskari_appsetup_bundles setup_bundles\n" +
            "    LEFT OUTER JOIN oskari_bundle bundle on setup_bundles.bundle_id = bundle.id\n" +
            "    LEFT OUTER JOIN oskari_appsetup setup ON setup_bundles.appsetup_id = setup.id\n" +
            "WHERE " +
            "   setup.type = 'PUBLISHED' AND " +
            "   bundle.name = 'mapfull' AND " +
            "   setup_bundles.config LIKE '%"+INFO_PLUGIN_CLASS+"%' AND " +
            "   setup_bundles.config LIKE '%colourScheme%';";

        JSONArray mapFullConfigs = new JSONArray();

        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    JSONObject mapfullConf = new JSONObject();
                    mapfullConf.put("appsetup_id", rs.getInt("appsetup_id"));
                    mapfullConf.put("bundle_id", rs.getInt("bundle_id"));
                    mapfullConf.put("config", rs.getString("config"));
                    mapfullConf.put("metadata", rs.getString("metadata"));
                    mapFullConfigs.put(mapfullConf);
                }
            } catch (JSONException e) {
                LOG.warn("Failed to fetch configs for migration ", e);
            }
        }
        return mapFullConfigs;
    }

    private JSONArray updateColourSchemeToTheme(JSONArray mapModules) throws JSONException {
        JSONArray returnValue = new JSONArray();

        for (int i = 0; i < mapModules.length(); i++) {
            JSONObject module = mapModules.getJSONObject(i);
            JSONObject config = new JSONObject(module.getString("config"));
            JSONObject metadata = new JSONObject(module.getString("metadata"));

            if(metadata == null || config == null) {
                continue;
            }

            JSONArray plugins = config.getJSONArray("plugins");

            for (int p = 0; p < plugins.length(); p++) {
                JSONObject plugin = plugins.getJSONObject(p);
                String id = plugin.getString("id");
                if (INFO_PLUGIN_CLASS.equals(id) && plugin.has("config")) {
                    JSONObject pluginConfig = plugin.getJSONObject("config");

                    if (pluginConfig.has("colourScheme")) {
                        JSONObject colourScheme = pluginConfig.getJSONObject("colourScheme");
                        String bgColour = null;
                        String titleColour = null;
                        if (colourScheme.has("bgColour")) {
                            bgColour = colourScheme.getString("bgColour");
                        }
                        if (colourScheme.has("titleColour")) {
                            titleColour = colourScheme.getString("titleColour");
                        }

                        if (bgColour != null || titleColour != null) {
                            if (!metadata.has("theme")) {
                                metadata.put("theme", new JSONObject());
                            }

                            JSONObject theme = metadata.getJSONObject("theme");
                            if (!theme.has("map")) {
                                theme.put("map", new JSONObject());
                            }

                            JSONObject map = theme.getJSONObject("map");
                            if (!map.has("infobox")) {
                                map.put("infobox", new JSONObject());
                            }

                            JSONObject infobox = map.getJSONObject("infobox");
                            JSONObject header = new JSONObject();
                            header.put("bg", bgColour);
                            header.put("text", titleColour);

                            infobox.put("header", header);

                            module.put("metadata", metadata.toString());

                        }
                        // remove colourscheme even when it's just an empty object.
                        pluginConfig.remove("colourScheme");
                        module.put("config", config.toString());
                        returnValue.put(module);
                    }
                }
            }

        }
        return returnValue;
    }
}
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
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class V3_1_1__remove_analysis_from_appsetups extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(flyway.oskari.V3_0_1__infobox_colourscheme_to_theme.class);
    private static final String ANALYSIS_LAYER_PLUGIN_ID = "Oskari.mapframework.bundle.mapanalysis.plugin.AnalysisLayerPlugin";
    private static final String ANALYSIS_LAYER_PREFIX = "analysis_";
    private static final int BATCH_SIZE = 1000;
    @Override
    public void migrate (Context context) throws Exception {
        Connection conn = context.getConnection();
        removeAnalysisLayerFromMapfullState(conn);
        removeAnalysisLayerPluginFromMapmodules(conn);
        removeAnalyseFromAppsetupBundles(conn);
        removeAnalyseFromBundles(conn);
    }

    private void removeAnalysisLayerFromMapfullState(Connection conn) throws SQLException {
        JSONArray mapfullConfigs = getMapfullConfigsWithAnalysisLayer(conn);
        String sql = """
            UPDATE oskari_appsetup_bundles bundles SET state = v.state 
            FROM (VALUES
                ${values}
            ) AS v(appsetup_id, bundle_id, state)
            WHERE bundles.appsetup_id = v.appsetup_id AND bundles.bundle_id = v.bundle_id
        """;

        List<String> values = new ArrayList<>();
        Statement statement = conn.createStatement();
        conn.setAutoCommit(false);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm:ss");
        LOG.debug(OffsetDateTime.now().format(dtf) + " Starting update.");
        for (int i = 0, counter = 0; i < mapfullConfigs.length(); i++, counter++) {
            JSONObject mapfull = mapfullConfigs.optJSONObject(i);
            JSONObject state = new JSONObject(mapfull.getString("state"));

            if (state == null || !state.has("selectedLayers")) {
                continue;
            }

            JSONArray selectedLayers = Optional.ofNullable(state.getJSONArray("selectedLayers")).orElse(new JSONArray());
            JSONArray newSelectedLayers = new JSONArray();
            for (int j = 0; j < selectedLayers.length(); j++) {
                JSONObject layer = selectedLayers.optJSONObject(j);
                if (layer != null) {
                    String id = layer.get("id").toString();
                    if (!id.startsWith(ANALYSIS_LAYER_PREFIX)) {
                        newSelectedLayers.put(layer);
                    }
                }
            }
            state.put("selectedLayers", newSelectedLayers);

            int appsetupId = mapfull.getInt("appsetup_id");
            int bundleId = mapfull.getInt("bundle_id");
            String valueString = "(" + appsetupId + ", " + bundleId + ", '" + state.toString() + "')";
            values.add(valueString);

            // execute once a batch size or when we reach the end
            if (counter == BATCH_SIZE - 1 || i == mapfullConfigs.length() - 1) {
                String query = sql.replace("${values}", String.join(",\n", values));
                statement.addBatch(query);
                counter = 0;
                values.clear();;
            }
        }

        int[] results = statement.executeBatch();
        LOG.debug("results: ", results.length);
        conn.commit();
        conn.setAutoCommit(true);
        LOG.debug(OffsetDateTime.now().format(dtf) + " Finished removing analysis layers from " + mapfullConfigs.length() + " views.");
    }


    private void removeAnalysisLayerPluginFromMapmodules(Connection conn) throws SQLException {
        JSONArray mapfullConfigs = getMapfullConfigsWithAnalysis(conn);
        String sql = """
            UPDATE oskari_appsetup_bundles bundles SET config = v.config 
            FROM (VALUES
                ${values}
            ) AS v(appsetup_id, bundle_id, config)
            WHERE bundles.appsetup_id = v.appsetup_id AND bundles.bundle_id = v.bundle_id
        """;

        List<String> values = new ArrayList<>();

        Statement statement = conn.createStatement();
        conn.setAutoCommit(false);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm:ss");
        LOG.debug(OffsetDateTime.now().format(dtf) + " Starting update.");
        for (int i = 0, counter = 0; i < mapfullConfigs.length(); i++, counter++) {
            JSONObject mapfull = mapfullConfigs.optJSONObject(i);
            JSONObject config = new JSONObject(mapfull.getString("config"));
            if (config != null) {
                JSONArray plugins = Optional.ofNullable(config.getJSONArray("plugins")).orElse(new JSONArray());
                JSONArray newPlugins = new JSONArray();
                for (int j = 0; j < plugins.length(); j++) {
                    JSONObject plugin = plugins.optJSONObject(j);
                    if (plugin != null) {
                        String id = plugin.getString("id");
                        if (id.indexOf(ANALYSIS_LAYER_PLUGIN_ID) == -1) {
                            newPlugins.put(plugin);
                        }
                    }
                }
                config.put("plugins", newPlugins);

                int appsetupId = mapfull.getInt("appsetup_id");
                int bundleId = mapfull.getInt("bundle_id");
                String valueString = "(" + appsetupId + ", " + bundleId + ", '" + config.toString() + "')";
                values.add(valueString);

                // execute once a batch size or when we reach the end
                if (counter == BATCH_SIZE - 1 || i == mapfullConfigs.length() - 1) {
                    String query = sql.replace("${values}", String.join(",\n", values));
                    statement.addBatch(query);
                    counter = 0;
                    values.clear();;
                }

            }
        }
        LOG.debug(OffsetDateTime.now().format(dtf) + " Finished processing the statements into batch: " + mapfullConfigs.length());

        int[] results = statement.executeBatch();
        LOG.debug("results: ", results.length);
        conn.commit();
        conn.setAutoCommit(true);
        LOG.debug(OffsetDateTime.now().format(dtf) + " Finished updating " + mapfullConfigs.length() + " views.");

    }

    private JSONArray getMapfullConfigsWithAnalysis(Connection conn) throws SQLException {
        final String sql = "SELECT appsetup_id, bundle_id, config FROM oskari_appsetup_bundles WHERE config LIKE '%AnalysisLayerPlugin%'";
        JSONArray mapFullConfigs = new JSONArray();

        try (final PreparedStatement statement = conn.prepareStatement(sql)) {

            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    JSONObject mapfullConf = new JSONObject();
                    mapfullConf.put("appsetup_id", rs.getInt("appsetup_id"));
                    mapfullConf.put("bundle_id", rs.getInt("bundle_id"));
                    mapfullConf.put("config", rs.getString("config"));
                    mapFullConfigs.put(mapfullConf);
                }
            } catch (JSONException e) {
                LOG.warn("Failed to fetch configs for migration ", e);
            }
        }

        return mapFullConfigs;
    }

    private JSONArray getMapfullConfigsWithAnalysisLayer(Connection conn) throws SQLException {
        final String sql = "SELECT appsetup_id, bundle_id, state FROM oskari_appsetup_bundles WHERE state LIKE '%selectedLayers%id%analysis%'";
        JSONArray mapFullConfigs = new JSONArray();

        try (final PreparedStatement statement = conn.prepareStatement(sql)) {

            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    JSONObject mapfullConf = new JSONObject();
                    mapfullConf.put("appsetup_id", rs.getInt("appsetup_id"));
                    mapfullConf.put("bundle_id", rs.getInt("bundle_id"));
                    mapfullConf.put("state", rs.getString("state"));
                    mapFullConfigs.put(mapfullConf);
                }
            } catch (JSONException e) {
                LOG.warn("Failed to fetch configs for migration ", e);
            }
        }

        return mapFullConfigs;
    }

    private void removeAnalyseFromAppsetupBundles(Connection conn) throws SQLException {
        final String sql = "DELETE FROM oskari_appsetup_bundles WHERE bundle_id = (SELECT id FROM oskari_bundle WHERE name = 'analyse')";
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.execute();
        }
    }

    private void removeAnalyseFromBundles(Connection conn) throws SQLException {
        final String sql = "DELETE FROM oskari_bundle WHERE name = 'analyse'";
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.execute();
        }
    }
}

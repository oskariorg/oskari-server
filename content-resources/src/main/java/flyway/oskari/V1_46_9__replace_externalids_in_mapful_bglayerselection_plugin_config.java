package flyway.oskari;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;

public class V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config implements JdbcMigration {

    // These don't need to be static, we expect this class to be instantiated only once
    private final Logger LOG = LogFactory.getLogger(this.getClass());
    private final String PLUGIN_ID = "Oskari.mapframework.bundle.mapmodule.plugin.BackgroundLayerSelectionPlugin";

    public void migrate(Connection conn) throws Exception {
        Integer mapfullBundleId = getMapfullBundleId(conn);
        if (mapfullBundleId == null) {
            LOG.info("Mapfull bundle not found");
            return;
        }
        int bundleId = mapfullBundleId;
        Map<String, Integer> externalIdToLayerId = getExternalIds(conn);
        List<BundleConfig> bundleConfigs = getBundleConfigs(conn, bundleId);
        List<BundleConfig> toUpdate = getBundleConfigsToUpdate(bundleConfigs, externalIdToLayerId);
        updateBundleConfigs(conn, toUpdate, bundleId);
    }

    private Integer getMapfullBundleId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM portti_bundle WHERE name = 'mapfull'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    private Map<String, Integer> getExternalIds(Connection conn) throws SQLException {
        String sql = "SELECT maplayerid, externalid FROM oskari_maplayer_externalid";
        Map<String, Integer> externalIdToLayerId = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int layerId = rs.getInt("maplayerid");
                String externalId = rs.getString("externalid");
                externalIdToLayerId.put(externalId, layerId);
            }
        }
        return null;
    }

    private List<BundleConfig> getBundleConfigs(Connection conn, int mapfullBundleId) throws SQLException {
        String sql = "SELECT view_id, seqno, config FROM portti_view_bundle_seq WHERE bundle_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, mapfullBundleId);
            try (ResultSet rs = statement.executeQuery()) {
                List<BundleConfig> configs = new ArrayList<>();
                while (rs.next()) {
                    BundleConfig config = new BundleConfig();
                    config.viewId = rs.getInt("view_id");
                    config.seqNo = rs.getInt("seqno");
                    config.config = JSONHelper.createJSONObject(rs.getString("config"));
                    configs.add(config);
                }
                return configs;
            }
        }
    }

    private List<BundleConfig> getBundleConfigsToUpdate(List<BundleConfig> bundleConfigs,
            Map<String, Integer> externalIdToLayerId) {
        List<BundleConfig> toUpdate = new ArrayList<>();

        for (BundleConfig bundleConfig : bundleConfigs) {
            try {
                JSONArray plugins = bundleConfig.config.optJSONArray("plugins");
                JSONObject bgPlugin = findBGPlugin(plugins);
                if (bgPlugin == null) {
                    continue;
                }
                JSONObject bgPluginConfig = bgPlugin.optJSONObject("config");
                if (bgPluginConfig == null) {
                    continue;
                }
                JSONArray baseLayers = bgPlugin.optJSONArray("baseLayers");
                boolean replacedSomething = replaceExternalIds(baseLayers, externalIdToLayerId);
                if (replacedSomething) {
                    toUpdate.add(bundleConfig);
                }
            } catch (JSONException e) {
                LOG.debug(e);
            }
        }

        return toUpdate;
    }

    private JSONObject findBGPlugin(JSONArray plugins) {
        if (plugins == null) {
            return null;
        }
        for (int i = 0; i < plugins.length(); i++) {
            JSONObject plugin = plugins.optJSONObject(i);
            if (plugin != null) {
                if (PLUGIN_ID.equals(plugin.optString("id"))) {
                    return plugin;
                }
            }
        }
        return null;
    }

    private boolean replaceExternalIds(JSONArray baseLayers,
            Map<String, Integer> externalIdToLayerId) throws JSONException {
        if (baseLayers == null) {
            return false;
        }
        boolean replaced = false;
        for (int i = 0; i < baseLayers.length(); i++) {
            String layerId = baseLayers.getString(i);
            if (ConversionHelper.getInt(layerId, -1) == -1) {
                // Not a number, possibly external id
                Integer id = externalIdToLayerId.get(layerId);
                if (id != null) {
                    baseLayers.put(i, id.toString());
                    replaced = true;
                }
            }
        }
        return replaced;
    }

    private void updateBundleConfigs(Connection conn, List<BundleConfig> bundleConfigs,
            int mapfullBundleId) throws SQLException {
        final boolean oldAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE portti_view_bundle_seq SET config=? WHERE bundle_id=? AND view_id=? AND seqno=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(2, mapfullBundleId);
                for (BundleConfig bundleConfig : bundleConfigs) {
                    ps.setString(1, bundleConfig.config.toString());
                    ps.setInt(3, bundleConfig.viewId);
                    ps.setInt(4, bundleConfig.seqNo);
                    ps.addBatch();
                    LOG.debug(ps.toString());
                }
                ps.executeBatch();
                conn.commit();
            }
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    class BundleConfig {
        int viewId;
        int seqNo;
        JSONObject config;
    }

}
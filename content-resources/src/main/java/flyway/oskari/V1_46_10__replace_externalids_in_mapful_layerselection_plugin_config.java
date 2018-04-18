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

public class V1_46_10__replace_externalids_in_mapful_layerselection_plugin_config implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_46_10__replace_externalids_in_mapful_layerselection_plugin_config.class);
    private static final String PLUGIN_ID = "Oskari.mapframework.bundle.mapmodule.plugin.LayerSelectionPlugin";

    public void migrate(Connection conn) throws Exception {
        Integer mapfullBundleId = getMapfullBundleId(conn);
        if (mapfullBundleId == null) {
            LOG.info("Mapfull bundle not found");
            return;
        }
        int bundleId = mapfullBundleId;
        LOG.debug("Mapfull bundle id:", bundleId);
        Map<String, Integer> externalIdToLayerId = getExternalIds(conn);
        List<BundleConfig> bundleConfigs = getBundleConfigs(conn, bundleId);
        List<BundleConfig> toUpdate = getBundleConfigsToUpdate(bundleConfigs, externalIdToLayerId);
        update(conn, toUpdate, bundleId);
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
        Map<String, Integer> externalIdToLayerId = new HashMap<>();

        String sql = "SELECT maplayerid, externalid FROM oskari_maplayer_externalid";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int layerId = rs.getInt("maplayerid");
                String externalId = rs.getString("externalid");
                LOG.debug("externalId:", externalId, "layerId:", layerId);
                externalIdToLayerId.put(externalId, layerId);
            }
        }

        return externalIdToLayerId;
    }

    private List<BundleConfig> getBundleConfigs(Connection conn, int mapfullBundleId) throws SQLException {
        List<BundleConfig> configs = new ArrayList<>();

        String sql = "SELECT view_id, seqno, config FROM portti_view_bundle_seq WHERE bundle_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, mapfullBundleId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    BundleConfig config = new BundleConfig();
                    config.viewId = rs.getInt("view_id");
                    config.seqNo = rs.getInt("seqno");
                    config.config = JSONHelper.createJSONObject(rs.getString("config"));
                    configs.add(config);
                }
            }
        }

        return configs;
    }

    protected static List<BundleConfig> getBundleConfigsToUpdate(List<BundleConfig> bundleConfigs,
            Map<String, Integer> externalIdToLayerId) {
        List<BundleConfig> toUpdate = new ArrayList<>();

        for (BundleConfig bundleConfig : bundleConfigs) {
            boolean updatedConfig = updateConfig(bundleConfig.config, externalIdToLayerId);
            if (updatedConfig) {
                toUpdate.add(bundleConfig);
            }
        }

        return toUpdate;
    }

    protected static boolean updateConfig(JSONObject config, Map<String, Integer> externalIdToLayerId) {
        if (config == null) {
            return false;
        }
        try {
            JSONArray plugins = config.optJSONArray("plugins");
            JSONObject plugin = findPlugin(plugins);
            if (plugin == null) {
                return false;
            }
            JSONObject layerSelectionPluginConfig = plugin.optJSONObject("config");
            if (layerSelectionPluginConfig == null) {
                return false;
            }

            boolean changedDefaultBaseLayer = replaceDefaultBaseLayer(layerSelectionPluginConfig, externalIdToLayerId);
            boolean changedAnyBaseLayer = replaceBaseLayers(layerSelectionPluginConfig, externalIdToLayerId);

            return changedDefaultBaseLayer || changedAnyBaseLayer;
        } catch (JSONException e) {
            LOG.warn(e);
            return false;
        }
    }

    protected static JSONObject findPlugin(JSONArray plugins) {
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

    private static boolean replaceDefaultBaseLayer(JSONObject plugin,
            Map<String, Integer> externalIdToLayerId) throws JSONException {
        String defaultBaseLayer = plugin.optString("defaultBaseLayer");
        Integer id = getId(defaultBaseLayer, externalIdToLayerId);
        if (id == null) {
            return false;
        }
        plugin.put("defaultBaseLayer", id.toString());
        return true;
    }

    private static boolean replaceBaseLayers(JSONObject plugin,
            Map<String, Integer> externalIdToLayerId) throws JSONException {
        JSONArray baseLayers = plugin.optJSONArray("baseLayers");
        if (baseLayers == null) {
            return false;
        }
        boolean replacedAtLeastOne = false;
        for (int i = 0; i < baseLayers.length(); i++) {
            String layerId = baseLayers.getString(i);
            Integer newLayerId = getId(layerId, externalIdToLayerId);
            if (newLayerId != null) {
                baseLayers.put(i, newLayerId.toString());
                replacedAtLeastOne = true;
            }
        }
        return replacedAtLeastOne;
    }

    /**
     * @return null if shouldn't be replaced, otherwise the new value
     */
    protected static Integer getId(String oldId, Map<String, Integer> externalIdToLayerId) {
        if (ConversionHelper.getInt(oldId, -1) == -1) {
            return externalIdToLayerId.get(oldId);
        }
        return null;
    }

    private void update(Connection conn, List<BundleConfig> bundleConfigs,
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

    protected static class BundleConfig {
        int viewId;
        int seqNo;
        JSONObject config;
    }

}

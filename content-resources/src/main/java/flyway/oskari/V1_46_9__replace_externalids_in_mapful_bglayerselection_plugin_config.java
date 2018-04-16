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

    private static final Logger LOG = LogFactory.getLogger(V1_46_9__replace_externalids_in_mapful_bglayerselection_plugin_config.class);
    private static final String PLUGIN_ID = "Oskari.mapframework.bundle.mapmodule.plugin.BackgroundLayerSelectionPlugin";

    public void migrate(Connection conn) throws Exception {
        Integer mapfullBundleId = getMapfullBundleId(conn);
        if (mapfullBundleId == null) {
            LOG.info("Mapfull bundle not found");
            return;
        }
        int bundleId = mapfullBundleId;
        LOG.debug("Mapfull bundle id:", bundleId);
        Map<String, Integer> externalIdToLayerId = getExternalIds(conn);
        List<BundleConfigNState> bundleConfigs = getBundleConfigs(conn, bundleId);
        List<BundleConfigNState> toUpdate = getBundleConfigsToUpdate(bundleConfigs, externalIdToLayerId);
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

    private List<BundleConfigNState> getBundleConfigs(Connection conn, int mapfullBundleId) throws SQLException {
        List<BundleConfigNState> configs = new ArrayList<>();

        String sql = "SELECT view_id, seqno, config, state FROM portti_view_bundle_seq WHERE bundle_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, mapfullBundleId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    BundleConfigNState config = new BundleConfigNState();
                    config.viewId = rs.getInt("view_id");
                    config.seqNo = rs.getInt("seqno");
                    config.config = JSONHelper.createJSONObject(rs.getString("config"));
                    config.state = JSONHelper.createJSONObject(rs.getString("state"));
                    configs.add(config);
                }
            }
        }

        return configs;
    }

    protected static List<BundleConfigNState> getBundleConfigsToUpdate(List<BundleConfigNState> bundleConfigs,
            Map<String, Integer> externalIdToLayerId) {
        List<BundleConfigNState> toUpdate = new ArrayList<>();

        for (BundleConfigNState bundleConfig : bundleConfigs) {
            boolean updatedConfig = updateConfig(bundleConfig.config, externalIdToLayerId);
            boolean updatedState = updateState(bundleConfig.state, externalIdToLayerId);
            if (updatedConfig || updatedState) {
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
            JSONObject bgPlugin = findBGPlugin(plugins);
            if (bgPlugin == null) {
                return false;
            }
            JSONObject bgPluginConfig = bgPlugin.optJSONObject("config");
            if (bgPluginConfig == null) {
                return false;
            }
            JSONArray baseLayers = bgPluginConfig.optJSONArray("baseLayers");
            return replaceExternalIds(baseLayers, externalIdToLayerId);
        } catch (JSONException e) {
            LOG.warn(e);
            return false;
        }
    }

    protected static JSONObject findBGPlugin(JSONArray plugins) {
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

    private static boolean replaceExternalIds(JSONArray baseLayers,
            Map<String, Integer> externalIdToLayerId) throws JSONException {
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

    protected static boolean updateState(JSONObject state, Map<String, Integer> externalIdToLayerId) {
        if (state == null) {
            return false;
        }
        try {
            JSONArray selectedLayers = state.optJSONArray("selectedLayers");
            if (selectedLayers == null) {
                return false;
            }
            boolean replacedAtLeastOne = false;
            for (int i = 0; i < selectedLayers.length(); i++) {
                JSONObject selectedLayer = selectedLayers.optJSONObject(i);
                Object layerId = selectedLayer.opt("id");
                if (layerId != null && layerId instanceof String) {
                    Integer newLayerId = getId((String) layerId, externalIdToLayerId);
                    if (newLayerId != null) {
                        selectedLayer.put("id", newLayerId);
                        replacedAtLeastOne = true;
                    }
                }
            }
            return replacedAtLeastOne;
        } catch (JSONException e) {
            LOG.warn(e);
            return false;
        }
    }

    private void update(Connection conn, List<BundleConfigNState> bundleConfigs,
            int mapfullBundleId) throws SQLException {
        final boolean oldAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE portti_view_bundle_seq SET config=?,state=? WHERE bundle_id=? AND view_id=? AND seqno=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(3, mapfullBundleId);
                for (BundleConfigNState bundleConfig : bundleConfigs) {
                    ps.setString(1, bundleConfig.config.toString());
                    ps.setString(2, bundleConfig.state.toString());
                    ps.setInt(4, bundleConfig.viewId);
                    ps.setInt(5, bundleConfig.seqNo);
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

    protected static class BundleConfigNState {
        int viewId;
        int seqNo;
        JSONObject config;
        JSONObject state;
    }

}
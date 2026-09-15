package flyway.myfeatures_userlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.BundleHelper;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class V1_1__migrate_published_userlayers extends BaseJavaMigration {

    private static final String TABLE_MYFEATURES_USERLAYER = V1_0__migrate_userlayers.TABLE_MYFEATURES_USERLAYER;
    private static final String MAPFULL = "mapfull";

    private static final Logger LOG = LogFactory.getLogger(V1_1__migrate_published_userlayers.class);

    public void migrate(Context ctx) throws Exception {
        DatasourceHelper helper = DatasourceHelper.getInstance();
        List<UserLayerIdToLayerId> mapping = null;
        try (Connection c = helper.getDataSource(helper.getOskariDataSourceName("myfeatures")).getConnection()) {
            mapping = getMapping(c);
        }
        if (mapping.isEmpty()) {
            return;
        }
        try (Connection c = helper.getDataSource().getConnection()) {
            replaceUserLayerIdWithLayerIdInPublishedMaps(c, mapping);
        }
    }

    private static List<UserLayerIdToLayerId> getMapping(Connection c) throws Exception {
        String sql = "SELECT user_layer_id, layer_id FROM " + TABLE_MYFEATURES_USERLAYER + " ORDER BY user_layer_id";
        List<UserLayerIdToLayerId> result = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int userLayerId = rs.getInt("user_layer_id");
                UUID layerId = rs.getObject("layer_id", UUID.class);
                result.add(new UserLayerIdToLayerId(userLayerId, layerId));
            }
        }
        return result;
    }

    private static void replaceUserLayerIdWithLayerIdInPublishedMaps(Connection c, List<UserLayerIdToLayerId> mapping) throws Exception {
        List<Integer> appsetupIdsToCheck = getAppsetupIds(c);
        for (int appsetupId : appsetupIdsToCheck) {
            Bundle mapfull = AppSetupHelper.getAppBundle(c, appsetupId, MAPFULL);
            boolean edited = replaceUserLayersWithMyFeatures(mapfull, mapping);
            if (edited) {
                AppSetupHelper.updateAppBundle(c, appsetupId, mapfull);
            }
        }
    }

    private static boolean replaceUserLayersWithMyFeatures(Bundle mapfull, List<UserLayerIdToLayerId> mapping) {
        // state.selectedLayers and conf.layers share similiar structure, so we can use single function for both
        JSONArray selectedLayers = mapfull.getStateJSON().optJSONArray("selectedLayers");
        JSONArray layers = mapfull.getConfigJSON().optJSONArray("layers");
        boolean a = replaceReferencedUserLayers(selectedLayers, mapping);
        boolean b = replaceReferencedUserLayers(layers, mapping);
        return a || b;
    }

    private static boolean replaceReferencedUserLayers(JSONArray arr, List<UserLayerIdToLayerId> mapping) {
        if (arr == null) {
            return false;
        }
        boolean edited = false;
        for (int i = 0, n = arr.length(); i < n; i++) {
            if (replaceReferencedUserLayer(arr.optJSONObject(i), mapping)) {
                edited = true;
            }
        }
        return edited;
    }

    private static boolean replaceReferencedUserLayer(JSONObject layer, List<UserLayerIdToLayerId> mapping) {
        if (layer == null) {
            return false;
        }
        String id = layer.optString("id");
        if (id == null || !id.startsWith("userlayer_")) {
            return false;
        }
        int userLayerId = Integer.parseInt(id.substring("userlayer_".length()));
        Optional<UUID> layerId = mapping.stream()
            .filter(x -> x.userLayerId == userLayerId)
            .map(x -> x.layerId)
            .findAny();
        if (layerId.isEmpty()) {
            return false;
        }
        layer.put("id", "myf_" + layerId.get());
        return true;
    }

    private static List<Integer> getAppsetupIds(Connection c) throws Exception {
        long mapfullBundleId = BundleHelper.getRegisteredBundle(c, MAPFULL).getBundleId();

        String sql = "SELECT DISTINCT appsetup_id"
            + " FROM oskari_appsetup_bundles"
            + " WHERE bundle_id = ? AND config::json->>'layers' LIKE '%userlayer%'";

        List<Integer> appsetupIdsToCheck = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, mapfullBundleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appsetupIdsToCheck.add(rs.getInt("appsetup_id"));
                }
            }
        }
        return appsetupIdsToCheck;
    }

    record UserLayerIdToLayerId(int userLayerId, UUID layerId) { }

}

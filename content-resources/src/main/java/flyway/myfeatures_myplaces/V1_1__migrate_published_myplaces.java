package flyway.myfeatures_myplaces;

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

public class V1_1__migrate_published_myplaces extends BaseJavaMigration {

    private static final String TABLE_MYFEATURES_CATEGORIES = V1_0__migrate_myplaces.TABLE_MYFEATURES_CATEGORIES;
    private static final String MAPFULL = "mapfull";

    private static final Logger LOG = LogFactory.getLogger(V1_1__migrate_published_myplaces.class);

    public void migrate(Context ctx) throws Exception {
        DatasourceHelper helper = DatasourceHelper.getInstance();
        List<CategoryIdToLayerId> mapping = null;
        try (Connection c = helper.getDataSource(helper.getOskariDataSourceName("myfeatures")).getConnection()) {
            mapping = getMapping(c);
        }
        if (mapping.isEmpty()) {
            return;
        }
        try (Connection c = helper.getDataSource().getConnection()) {
            replaceCategoryIdWithLayerIdInPublishedMaps(c, mapping);
        }
    }

    private static List<CategoryIdToLayerId> getMapping(Connection c) throws Exception {
        String sql = "SELECT category_id, layer_id FROM " + TABLE_MYFEATURES_CATEGORIES + " ORDER BY category_id";
        List<CategoryIdToLayerId> result = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int categoryId = rs.getInt("category_id");
                UUID layerId = rs.getObject("layer_id", UUID.class);
                result.add(new CategoryIdToLayerId(categoryId, layerId));
            }
        }
        return result;
    }

    private static void replaceCategoryIdWithLayerIdInPublishedMaps(Connection c, List<CategoryIdToLayerId> mapping) throws Exception {
        List<Integer> appsetupIdsToCheck = getAppsetupIds(c);
        for (int appsetupId : appsetupIdsToCheck) {
            Bundle mapfull = AppSetupHelper.getAppBundle(c, appsetupId, MAPFULL);
            boolean edited = replaceMyPlacesWithMyFeatures(mapfull, mapping);
            if (edited) {
                AppSetupHelper.updateAppBundle(c, appsetupId, mapfull);
            }
        }
    }

    private static boolean replaceMyPlacesWithMyFeatures(Bundle mapfull, List<CategoryIdToLayerId> mapping) {
        // state.selectedLayers and conf.layers share similiar structure, so we can use single function for both
        JSONArray selectedLayers = mapfull.getStateJSON().optJSONArray("selectedLayers");
        JSONArray layers = mapfull.getConfigJSON().optJSONArray("layers");
        boolean a = replaceReferencedMyPlacesLayers(selectedLayers, mapping);
        boolean b = replaceReferencedMyPlacesLayers(layers, mapping);
        return a || b;
    }

    private static boolean replaceReferencedMyPlacesLayers(JSONArray arr, List<CategoryIdToLayerId> mapping) {
        if (arr == null) {
            return false;
        }
        boolean edited = false;
        for (int i = 0, n = arr.length(); i < n; i++) {
            if (replaceReferencedMyPlacesLayer(arr.optJSONObject(i), mapping)) {
                edited = true;
            }
        }
        return edited;
    }

    private static boolean replaceReferencedMyPlacesLayer(JSONObject layer, List<CategoryIdToLayerId> mapping) {
        if (layer == null) {
            return false;
        }
        String id = layer.optString("id");
        if (id == null || !id.startsWith("myplaces_")) {
            return false;
        }
        int categoryId = Integer.parseInt(id.substring("myplaces_".length()));
        Optional<UUID> layerId = mapping.stream()
            .filter(x -> x.categoryId == categoryId)
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
            + " WHERE bundle_id = ? AND config::json->>'layers' LIKE '%myplaces%'";

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

    record CategoryIdToLayerId(int categoryId, UUID layerId) { }

}

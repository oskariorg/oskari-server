package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrates filtered regions (not supported yet) in bundle states legacy-block from sotkanet regions ids to features in regionsets.
 */
public class V1_45_16__migrate_legacy_filtered_region_ids implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_45_16__migrate_legacy_filtered_region_ids.class);

    private Map<Integer, String> sotkaIdToRegionId = new HashMap<>();

    class Bundle {
        int view;
        int bundle;
        String state;
    }

    public void migrate(Connection conn) throws SQLException, IOException, JSONException {
        populateRegionMapping();
        List<Bundle> bundles = getStatesWithLegacyFilterRegions(conn);
        for(Bundle bundle : bundles) {
            if(migrateData(bundle)) {
                updateData(conn, bundle);
            }
        }
    }

    private List<Bundle> getStatesWithLegacyFilterRegions(Connection conn) throws SQLException {
        String sql = "SELECT view_id, bundle_id, state FROM portti_view_bundle_seq where bundle_id = (select id from portti_bundle where name = 'statsgrid') and state LIKE '%\"legacy\"%';";
        List<Bundle> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    Bundle ind = new Bundle();
                    ind.view = rs.getInt("view_id");
                    ind.bundle = rs.getInt("bundle_id");
                    ind.state = rs.getString("state");
                    list.add(ind);
                }
            }
        }
        return list;
    }

    private void populateRegionMapping() throws IOException, JSONException {
        final String json = IOHelper.readString(getClass().getResourceAsStream("NonFlywayResource_1_45_14_regions.json"));
        JSONArray sotkaRegions = new JSONArray(json);
        for (int i = 0; i < sotkaRegions.length(); ++i) {
            JSONObject region = sotkaRegions.getJSONObject(i);
            sotkaIdToRegionId.put(region.getInt("id"), region.getString("code"));
        }
    }

    /**
     * Returns true if bundle was migrated and false if no update is required
     * @param bundle
     * @return
     * @throws SQLException
     */
    private boolean migrateData(Bundle bundle) {
        JSONObject state = null;
        JSONObject legacyInfo;
        JSONArray sotkanetRegions = null;
        try {
            state = new JSONObject(bundle.state);
            legacyInfo = state.optJSONObject("legacy");
            // for some reason the array is as string in the state
            sotkanetRegions = new JSONArray(legacyInfo.optString("sotkanetRegions"));
        } catch (JSONException ex) {
            LOG.warn("Bundle didn't have expected legacy info - skipping view:", bundle.view);
            return false;
        }
        // migrate data from sotkanet region ids to regionset feature ids and format data to new object format
        // from [list of sotkanetRegions] to [list of feature ids in regionset layer]
        JSONArray migratedRegionIds = new JSONArray();
        for (int i = 0; i < sotkanetRegions.length(); ++i) {
            int region = sotkanetRegions.optInt(i, -1);
            if(region == -1) {
                continue;
            }
            // sotkanet had integers, but region ids can be something like "005" so using strings
            String regionCode = sotkaIdToRegionId.get(region);
            if(regionCode != null) {
                migratedRegionIds.put(regionCode);
            }
        }
        legacyInfo.remove("sotkanetRegions");
        JSONHelper.put(legacyInfo, "regions", migratedRegionIds);
        bundle.state = state.toString();
        return migratedRegionIds.length() > 0;
    }

    /**
     * Updates bundle state (called for migrated bundles)
     */
    private static void updateData(Connection conn, Bundle data) throws SQLException {

        final String sql ="UPDATE portti_view_bundle_seq SET " +
                "state = ? " +
                "WHERE view_id = ? AND bundle_id = ?";
        try(final PreparedStatement statement =
                    conn.prepareStatement(sql)) {
            statement.setString(1, data.state);
            statement.setInt(2, data.view);
            statement.setInt(3, data.bundle);
            statement.execute();
        }
    }
}
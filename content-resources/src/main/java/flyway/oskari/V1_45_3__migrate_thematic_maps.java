package flyway.oskari;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;

/**
 * Migrate conf and state in portti_view_bundle_seq for
 * statsgrid and publishedgrid bundles
 *
 * @see https://github.com/oskariorg/oskari-frontend/blob/master/bundles/statistics/statsgrid/plugin/ManageClassificationPlugin.js
 */
public class V1_45_3__migrate_thematic_maps implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_45_3__migrate_thematic_maps.class);

    private static final String BUNDLE_NAME_STATSGRID = "statsgrid";
    private static final String BUNDLE_NAME_PUBLISHEDGRID = "publishedgrid";

    private ThematicMapsRegionsetHelper regionsetHelper;

    public void migrate(Connection conn) throws SQLException, JSONException {
        regionsetHelper = new ThematicMapsRegionsetHelper(conn);
        final boolean oldAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            migrateBundle(conn, BUNDLE_NAME_STATSGRID);
            migrateBundle(conn, BUNDLE_NAME_PUBLISHEDGRID);
            // conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }


    private void migrateBundle(Connection conn, String bundleName)
            throws SQLException, JSONException {
        long bundleId = ThematicMapsViewHelper.getBundleId(conn, bundleName);
        if (bundleId < 0) {
            LOG.warn("Could not find bundle by name:", bundleName);
            return;
        }
        List<ConfigNState> configsAndStates = ThematicMapsViewHelper.getConfigsAndStates(conn, bundleId);
        for (ConfigNState configAndState : configsAndStates) {
            migrate(configAndState);
        }
        ThematicMapsViewHelper.update(conn, configsAndStates);
    }



    private void migrate(ConfigNState configAndState)
            throws JSONException {
        JSONObject config = new JSONObject(configAndState.config);
        JSONObject state = new JSONObject(configAndState.state);

        config = migrateConfig(config, state);
        state = migrateState(config, state);

        configAndState.config = config.toString();
        configAndState.state = state.toString();
    }

    private JSONObject migrateConfig(JSONObject config, JSONObject state)
            throws JSONException {
        JSONObject newConfig = new JSONObject();
        newConfig.put("grid", state.optBoolean("gridShown"));
        newConfig.put("legendLocation", "top right");
        newConfig.put("allowClassification", config.optBoolean("allowClassification"));
        return newConfig;
    }

    private JSONObject migrateState(JSONObject config, JSONObject state) throws JSONException {
        JSONObject newState = new JSONObject();

        // regionset
        String regionCategory = JSONHelper.getStringFromJSON(state, "regionCategory", "KUNTA");
        Integer regionsLayerId = regionsetHelper.getLayerIdForName(regionCategory);
        if (regionsLayerId == null) {
            LOG.error("Could not find layerId for category:", regionCategory);
            throw new IllegalArgumentException("Could not find layerId for category:" + regionCategory);
        }
        newState.put("regionset", regionsLayerId);


        // OLD: "currentColumn": "indicator2882013total", // "indicator" + id + year + male/female/total
        // NEW: "active" : "1_4_sex="total":year="2016"" // ds_id + '_' + ind_id + '_' + [alphabetical order for selections] key + '=' + value [separated by] ':'

        // Old thematic map supports only one indicator at a time
        // so the same classification will be used for all indicators
        JSONObject classification = migrateClassification(state);

        // Indicators
        JSONArray indicators = state.getJSONArray("indicators");
        List<JSONObject> newIndicators = migrateIndicators(indicators);
        for (JSONObject indicator : newIndicators) {
            indicator.put("classification", classification);
        }
        // Active indicator

        return newState;
    }

    private List<JSONObject> migrateIndicators(JSONArray indicators) {
        // TODO Auto-generated method stub
        // ds -> default to sotkanet or own indicators
        return null;
    }

    private JSONObject migrateClassification(JSONObject state) throws JSONException {
        int methodId = Integer.parseInt(state.getString("methodId"));
        String method = getMethodFromMethodId(methodId);

        int numberOfClasses = 0;
        if (state.has("numberOfClasses")) {
            try {
                numberOfClasses = state.getInt("numberOfClasses");
            } catch (JSONException ignore) {}
        }

        String classificationMode = state.getString("classificationMode");

        JSONObject classification = new JSONObject();
        classification.put("method", method);
        classification.put("count", numberOfClasses);
        classification.put("mode", classificationMode); // "discontinuous" or "distinct"
        classification.put("mapStyle", "choropleth"); // old had only choro so default to it

        if (state.has("colors")) {
            JSONObject colors = state.getJSONObject("colors");
            String set = colors.getString("set");
            int index = colors.getInt("index");
            classification.put("type", set);
            classification.put("name", ThematicMapsColorHelper.getColorNameFromIndex(set, index));
            classification.put("reverseColors", colors.optBoolean("flipped"));
        }

        return classification;
    }

    private String getMethodFromMethodId(int methodId) {
        // method : ['jenks', 'quantile', 'equal'], // , 'manual'
        switch (methodId) {
            case 1: return "jenks";
            case 2: return "quantile";
            case 3: return "equal";
            case 4: return "manual";
            default: return null;
        }
    }



}
package flyway.oskari;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
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
 * Required configuration:
 * ----------------------------------
 # datasource ids added for old indicators
 # [SELECT id FROM oskari_statistical_datasource where config like '%sotkanet.fi/rest%']
 flyway.1_45_4.sotkanet.ds.id=1
 # [SELECT id FROM oskari_statistical_datasource where plugin = 'UserStats']
 flyway.1_45_4.userindicators.ds.id=4

 # layer name mappings for old regionsets
 # [select name from oskari_maplayer where type = 'statslayer']
 flyway.1_45_4.layer.name.kunta=tilastointialueet:kunta4500k_2017
 flyway.1_45_4.layer.name.aluehallintovirasto=tilastointialueet:avi4500k
 flyway.1_45_4.layer.name.maakunta=tilastointialueet:maakunta4500k
 flyway.1_45_4.layer.name.seutukunta=tilastointialueet:seutukunta4500k
 flyway.1_45_4.layer.name.elykeskus=tilastointialueet:ely4500k
 flyway.1_45_4.layer.name.nuts1=dummy:nuts1
 flyway.1_45_4.layer.name.erva=dummy:erva
 flyway.1_45_4.layer.name.sairaanhoitopiiri=dummy:sairaanhoitopiiri
 * ----------------------------------
 *
 * @see https://github.com/oskariorg/oskari-frontend/blob/master/bundles/statistics/statsgrid/plugin/ManageClassificationPlugin.js
 */
@SuppressWarnings("JavadocReference")
public class V1_45_4__migrate_thematic_maps implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_45_4__migrate_thematic_maps.class);

    private static final String BUNDLE_NAME_STATSGRID = "statsgrid";
    private static final String BUNDLE_NAME_PUBLISHEDGRID = "publishedgrid";
    private static final String PROP_DS_SOTKA = "flyway.1_45_4.sotkanet.ds.id";
    private static final String PROP_DS_USER = "flyway.1_45_4.userindicators.ds.id";

    private ThematicMapsRegionsetHelper regionsetHelper;
    private long sotkanetId;
    private long userIndicatorsId;

    public void migrate(Connection conn) throws SQLException, JSONException {
        sotkanetId = PropertyUtil.getOptional(PROP_DS_SOTKA, -1);
        userIndicatorsId = PropertyUtil.getOptional(PROP_DS_USER, -1);
        final boolean oldAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            // migrate statsgrid to new one in portti_bundle
            // sending bundlePath to only migrate the ones with old statsgrid startup!!
            // update state and config from old to new model
            migrateBundle(conn, BUNDLE_NAME_STATSGRID, "/packages/statistics/bundle/");

            // update state and config from old to new model
            migrateBundle(conn, BUNDLE_NAME_PUBLISHEDGRID, null);

            ConfigNState divmanazer = ThematicMapsViewHelper.getBundle(conn, ThematicMapsViewHelper.getBundleId(conn, "divmanazer"));
            final long mapfullId = ThematicMapsViewHelper.getBundleId(conn, "mapfull");
            final long statsgridId = ThematicMapsViewHelper.getBundleId(conn, BUNDLE_NAME_STATSGRID);
            for(long viewId: ThematicMapsViewHelper.findAppsetupsHavingBundleButNoDivmanazer(conn, statsgridId)){
                ThematicMapsViewHelper.injectDivmanazerAfterMapfull(conn, viewId, divmanazer, mapfullId);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    private void migrateBundle(Connection conn, String bundleName, String bundlePath)
            throws SQLException, JSONException {
        long bundleId = ThematicMapsViewHelper.getBundleId(conn, bundleName);
        if (bundleId < 0) {
            LOG.warn("Could not find bundle by name:", bundleName);
            return;
        }
        List<ConfigNState> configsAndStates = ThematicMapsViewHelper.getConfigsAndStates(conn, bundleId, bundlePath);
        if(configsAndStates.isEmpty()) {
            return;
        }
        LOG.info("Found", configsAndStates.size(),"appsetups in need of migration for", bundleName, "bundle.");
        if(regionsetHelper == null) {
            // lazy init so instances without migration don't need the configs to pass this migration
            regionsetHelper = new ThematicMapsRegionsetHelper(conn);
        }
        for (ConfigNState configAndState : configsAndStates) {
            migrate(configAndState);
        }
        ThematicMapsViewHelper.update(conn, configsAndStates);
        final long statsgridId = ThematicMapsViewHelper.getBundleId(conn, BUNDLE_NAME_STATSGRID);
        // reset startup for statsgrid bundle to load the new code
        // publishedgrid bundle needs to be changed to statsgrid
        ThematicMapsViewHelper.switchBundle(conn, bundleId, statsgridId);
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

        // Old thematic map supports only one indicator at a time
        // so the same classification will be used for all indicators
        JSONObject classification = migrateClassification(state);

        // Indicators
        JSONArray indicators = state.optJSONArray("indicators");
        if(indicators == null) {
            // state is invalid if there's no indicators in it - reset it
            return new JSONObject();
        }
        List<JSONObject> newIndicators = migrateIndicators(indicators);
        String currentColumn = state.optString("currentColumn", "");
        for (JSONObject indicator : newIndicators) {
            indicator.put("classification", classification);
            // Active indicator
            String myOldHash = getCurrentColumnStr(indicator);
            if(currentColumn.equals(myOldHash) && !newState.has("active")) {
                newState.put("active", geIndicatorHash(indicator));
            }
        }
        // attach the migrated indicators to the new state
        newState.put("indicators", newIndicators);

        // legacy-block
        // things that we don't support yet, but we have a chance to restore later
        // NOTE! These will be removed if someone edits the map with the publisher tool
        JSONObject legacy = new JSONObject();
        // state.manualBreaksInput (if methodId == 4)
        if("4".equals(state.optString("methodId"))) {
            // 4 is/was the manual method and for some reason the number is saved as a string
            // value is like "0,50,70,100,300,700"
            legacy.put("manualBreaks", state.optString("manualBreaksInput"));
        }
        // state.municipalities
        JSONArray regionIds = state.optJSONArray("municipalities");
        if(regionIds != null && regionIds.length() > 0) {
            // the values are some internal sotkanet region ids, not something like municipality id
            legacy.put("sotkanetRegions", state.optString("municipalities"));
        }
        // if there's something to save from the legacy stuff, lets attach it to the new state
        if(legacy.length() > 0) {
            newState.put("legacy", legacy);
        }

        return newState;
    }

    // OLD: "currentColumn": "indicator2882013total"
    // "indicator" + id + year + male/female/total
    public static String getCurrentColumnStr(JSONObject indicator) throws JSONException {
        JSONObject selections = indicator.getJSONObject("selections");
        return "indicator" + indicator.optString("id") + selections.optString("year", "") +selections.optString("sex", "");
    }

    // NEW: "active" : "1_4_sex="total":year="2016""
    // ds_id + '_' + ind_id + '_' + [alphabetical order for selections] key + '=' + value [separated by] ':'
    public static String geIndicatorHash(JSONObject indicator) throws JSONException {
        JSONObject selections = indicator.getJSONObject("selections");
        StringBuilder idStr = new StringBuilder(indicator.optString("ds", ""));
        idStr.append("_");
        idStr.append(indicator.optString("id", ""));
        idStr.append("_");
        Iterator<String> it = selections.sortedKeys();
        while (it.hasNext()) {
            String key = it.next();
            idStr.append(key);
            idStr.append('=');
            idStr.append('"');
            try {
                idStr.append(selections.get(key));
            } catch (JSONException e) {
                // Ignore, we are iterating the keys, the key _does_ exist
            }
            idStr.append('"');
            if(it.hasNext()) {
                idStr.append(':');
            }
        }
        return idStr.toString();
    }


    private List<JSONObject> migrateIndicators(JSONArray indicators) {
        List<JSONObject> list = new ArrayList<>(indicators.length());
        for(int i = 0; i < indicators.length(); ++i) {
            JSONObject oldIndicator = indicators.optJSONObject(i);
            try {
                JSONObject newIndicator = new JSONObject();
                newIndicator.put("id", oldIndicator.getString("id"));
                JSONObject selections = new JSONObject();
                newIndicator.put("selections", selections);
                // ds -> default to sotkanet or own indicators
                if(oldIndicator.optBoolean("ownIndicator")) {
                    if(userIndicatorsId == -1L) {
                        LOG.warn("Datasource id for user indicators not configured. Provide datasource id as property value for", PROP_DS_USER);
                        throw new RuntimeException("Trying to migrate user indicator, but datasource id is not configured!");
                    }
                    newIndicator.put("ds", userIndicatorsId);
                } else {
                    if(sotkanetId == -1L) {
                        LOG.warn("Datasource id for sotkanet not configured. Provide datasource id as property value for", PROP_DS_SOTKA);
                        throw new RuntimeException("Trying to migrate sotkanet indicator, but datasource id is not configured!");
                    }
                    newIndicator.put("ds", sotkanetId);
                }
                selections.put("sex", oldIndicator.optString("gender"));
                selections.put("year", oldIndicator.optString("year"));
                list.add(newIndicator);
            } catch (JSONException e) {
                LOG.error("Couldn't migrate indicator:", oldIndicator, e.getMessage());
            }
        }
        return list;
    }

    private JSONObject migrateClassification(JSONObject state) throws JSONException {
        // default to "jenks"
        int methodId = ConversionHelper.getInt(state.optString("methodId", "1"), 1);
        String method = getMethodFromMethodId(methodId);

        int numberOfClasses = state.optInt("numberOfClasses", 0);

        // default to "distinct"
        String classificationMode = state.optString("classificationMode", "distinct");

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
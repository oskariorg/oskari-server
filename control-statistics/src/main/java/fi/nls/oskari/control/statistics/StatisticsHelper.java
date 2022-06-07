package fi.nls.oskari.control.statistics;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.data.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for GetIndicatorDataHandler
 *
 * Reason for a separate class is that GetIndicatorDataHandler
 * has a static class member that throws an exception while
 * instantiating if certain properties have not been set
 * causing unit tests that load the class to fail. If this
 * issue gets fixed feel free to move these functions around.
 */
public class StatisticsHelper {

    public static final String PARAM_DATASOURCE_ID = "datasource";
    public static final String PARAM_INDICATOR_ID = "indicator"; // previously indicator_id
    public static final String PARAM_SELECTORS = "selectors";
    public static final String PARAM_REGIONSET = "regionset";

    public static String getIndicatorMetadataCacheKey(long datasourceId, String indicatorId) {
        return "oskari_get_indicator_metadata_handler_" + datasourceId + ":" + indicatorId;
    }

    public static String getIndicatorDataCacheKey(long datasourceId, String indicatorId,
                                                  long layerId, JSONObject selectorJSON) {
        StringBuilder cacheKey = new StringBuilder("oskari:stats:");
        cacheKey.append(datasourceId);
        cacheKey.append(":data:");
        cacheKey.append(indicatorId);
        cacheKey.append(':');
        cacheKey.append(layerId);
        Iterator<String> it = selectorJSON.sortedKeys();
        while (it.hasNext()) {
            String key = it.next();
            cacheKey.append(':');
            cacheKey.append(key);
            cacheKey.append('=');
            try {
                cacheKey.append(selectorJSON.get(key));
            } catch (JSONException e) {
                // Ignore, we are iterating the keys, the key _does_ exist
            }
        }
        return cacheKey.toString();
    }

    public static void flushDataFromCache(long pluginId, String indicatorId, long layerId, JSONObject selectorJSON) {
        String cacheKey = getIndicatorDataCacheKey(pluginId, indicatorId, layerId, selectorJSON);
        JedisManager.del(cacheKey);
    }

    public static StatisticalIndicatorDataModel getIndicatorDataModel(JSONObject selectorJSON) {
        StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
        @SuppressWarnings("unchecked")
        Iterator<String> keys = selectorJSON.keys();
        while (keys.hasNext()) {
            try {
                String key = keys.next();
                String value = selectorJSON.getString(key);
                selectors.addDimension(new StatisticalIndicatorDataDimension(key, value));
            } catch (JSONException ignore) {
                // The key _does_ exist
            }
        }
        return selectors;
    }

    public static JSONObject toJSON(StatisticalIndicator indicator) throws JSONException {
        JSONObject pluginIndicatorJSON = new JSONObject();
        Map<String, String> name = indicator.getName();
        Map<String, String> description = indicator.getDescription();
        Map<String, String> source = indicator.getSource();
        List<StatisticalIndicatorLayer> layers = indicator.getLayers();
        StatisticalIndicatorDataModel selectors = indicator.getDataModel();

        pluginIndicatorJSON.put("id", indicator.getId());
        pluginIndicatorJSON.put("name", name);
        pluginIndicatorJSON.put("description", description);
        pluginIndicatorJSON.put("source", source);
        pluginIndicatorJSON.put("public", indicator.isPublic());
        pluginIndicatorJSON.put("regionsets", toJSON(layers));
        pluginIndicatorJSON.put("selectors", toJSON(selectors));
        pluginIndicatorJSON.put("metadata", indicator.getMetadata());
        pluginIndicatorJSON.put("created", indicator.getCreated());
        pluginIndicatorJSON.put("updated", indicator.getUpdated());
        return pluginIndicatorJSON;
    }

    public static JSONArray toJSON(StatisticalIndicatorDataModel selectors) throws JSONException {
        JSONArray selectorsJSON = new JSONArray();
        for (StatisticalIndicatorDataDimension selector : selectors.getDimensions()) {
            JSONObject selectorJSON = new JSONObject();
            selectorJSON.put("id", selector.getId());
            selectorJSON.put("name", selector.getName());
            if(selectors.isTimeVariable(selector)) {
                selectorJSON.put("time", true);
            }
            selectorJSON.put("allowedValues", toJSON(selector.getAllowedValues()));
            // Note: Values are not given here, they are null anyhow in this phase.
            selectorsJSON.put(selectorJSON);
        }
        return selectorsJSON;
    }

    private static JSONArray toJSON(Collection<IdNamePair> stringCollection) {
        JSONArray stringArray = new JSONArray();
        for (IdNamePair value : stringCollection) {
            stringArray.put(value.getValueForJson());
        }
        return stringArray;
    }

    public static JSONArray toJSON(List<StatisticalIndicatorLayer> layers) throws JSONException {
        JSONArray layersJSON = new JSONArray();
        for (StatisticalIndicatorLayer layer: layers) {
            layersJSON.put(layer.getOskariLayerId());
        }
        return layersJSON;
    }


}

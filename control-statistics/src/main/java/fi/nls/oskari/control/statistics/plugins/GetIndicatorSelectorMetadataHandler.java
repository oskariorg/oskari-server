package fi.nls.oskari.control.statistics.plugins;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.control.ActionParamsException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.ResponseHelper;

/**
 * This interface gives the relevant information for one indicator to the frontend.
 * This information can be subsequently used to query the actual indicator data.
 * 
 * - action_route=GetIndicatorSelectorMetadata
 * 
 * eg.
 * OSKARI_URL?action_route=GetIndicatorSelectorMetadata
 * Response is in JSON, and contains the indicator selector metadata for one indicator.
 */
@OskariActionRoute("GetIndicatorSelectorMetadata")
public class GetIndicatorSelectorMetadataHandler extends ActionHandler {
    private final static String CACHE_PREFIX = "oskari_get_indicator_selector_metadata_handler_";
    private final static String PARAM_PLUGIN_ID = "plugin_id";
    private final static String PARAM_INDICATOR_ID = "indicator_id";

    /**
     * For now, this uses pretty much static global store for the plugins.
     * In the future it might make sense to inject the pluginManager references to different controllers using DI.
     */
    private static final StatisticalDatasourcePluginManager pluginManager = StatisticalDatasourcePluginManager.getInstance();

    @Override
    public void handleAction(ActionParameters ap) throws ActionException {
        final long pluginId = ap.getRequiredParamInt(PARAM_PLUGIN_ID);
        final String indicatorId = ap.getRequiredParam(PARAM_INDICATOR_ID);
        JSONObject response = getIndicatorMetadataJSON(ap.getUser(), pluginId, indicatorId);
        ResponseHelper.writeResponse(ap, response);
    }
    
    /**
     * Requests new data skipping the cache. Used for cache refresh before expiration.
     * @return
     * @throws ActionException
     */
    JSONObject getIndicatorMetadataJSON(User user, long pluginId, String indicatorId) throws ActionException {
        StatisticalDatasourcePlugin plugin = pluginManager.getPlugin(pluginId);
        String cacheKey = CACHE_PREFIX + pluginId + ":" + indicatorId;
        if (plugin.canCache()) {
            final String cachedData = JedisManager.get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                try {
                    return new JSONObject(cachedData);
                } catch (JSONException e) {
                    // Failed serializing. Skipping the cache.
                }
            }
        }
        try {
            for (StatisticalIndicator indicator : plugin.getIndicators(user)) {
                if (indicator.getId().equals(indicatorId)) {
                    JSONObject indicatorMetadata = toJSON(indicator);
                    // Note that there is an another layer of caches in the plugins doing the web queries.
                    // Two layers are necessary, because deserialization and conversion to the internal data model
                    // is pretty heavy operation.
                    if (plugin.canCache()) {
                        JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, indicatorMetadata.toString());
                    }
                    return indicatorMetadata;
                }
            }
            return null;
        } catch (JSONException e) {
            throw new ActionException("Something went wrong in getting indicator metadata.", e);
        }
    }
    
    private JSONObject toJSON(StatisticalIndicator indicator) throws JSONException {
        JSONObject pluginIndicatorJSON = new JSONObject();
        Map<String, String> name = indicator.getLocalizedName();
        Map<String, String> description = indicator.getLocalizedDescription();
        Map<String, String> source = indicator.getLocalizedSource();
        List<StatisticalIndicatorLayer> layers = indicator.getLayers();
        StatisticalIndicatorSelectors selectors = indicator.getSelectors();
        
        pluginIndicatorJSON.put("name", name);
        pluginIndicatorJSON.put("description", description);
        pluginIndicatorJSON.put("source", source);
        pluginIndicatorJSON.put("public", indicator.isPublic());
        pluginIndicatorJSON.put("layers", toJSON(layers));
        pluginIndicatorJSON.put("selectors", toJSON(selectors));
        return pluginIndicatorJSON;
    }

    private JSONArray toJSON(StatisticalIndicatorSelectors selectors) throws JSONException {
        JSONArray selectorsJSON = new JSONArray();
        for (StatisticalIndicatorSelector selector : selectors.getSelectors()) {
            JSONObject selectorJSON = new JSONObject();
            selectorJSON.put("id", selector.getId());
            selectorJSON.put("allowedValues", toJSON(selector.getAllowedValues()));
            // Note: Values are not given here, they are null anyhow in this phase.
            selectorsJSON.put(selectorJSON);
        }
        return selectorsJSON;
    }

    private JSONArray toJSON(Collection<String> stringCollection) {
        JSONArray stringArray = new JSONArray();
        for (String value : stringCollection) {
            stringArray.put(value);
        }
        return stringArray;
    }

    private JSONArray toJSON(List<StatisticalIndicatorLayer> layers) throws JSONException {
        JSONArray layersJSON = new JSONArray();
        for (StatisticalIndicatorLayer layer: layers) {
            JSONObject layerJSON = new JSONObject();
            layerJSON.put("type", layer.getIndicatorValueType().toString());
            layerJSON.put("layerId", layer.getOskariLayerId());
            layersJSON.put(layerJSON);
        }
        return layersJSON;
    }

}

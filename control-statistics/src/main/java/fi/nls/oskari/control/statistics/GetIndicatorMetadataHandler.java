package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This interface gives the relevant information for one indicator to the frontend.
 * This information can be subsequently used to query the actual indicator data.
 * 
 * - action_route=GetIndicatorMetadataHandler
 * 
 * eg.
 * OSKARI_URL?action_route=GetIndicatorSelectorMetadata?datasource=1&indicator=2
 * Response is in JSON, and contains the indicator selector metadata for one indicator.
 */
@OskariActionRoute("GetIndicatorMetadata")
public class GetIndicatorMetadataHandler extends ActionHandler {
    private final static String CACHE_PREFIX = "oskari_get_indicator_metadata_handler_";
    private final static String PARAM_PLUGIN_ID = "datasource";
    private final static String PARAM_INDICATOR_ID = "indicator";

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
    public JSONObject getIndicatorMetadataJSON(User user, long pluginId, String indicatorId) throws ActionException {
        StatisticalDatasourcePlugin plugin = pluginManager.getPlugin(pluginId);
        if(plugin == null) {
            throw new ActionParamsException("No such datasource: " + pluginId);
        }
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
        StatisticalIndicator indicator = plugin.getIndicator(user, indicatorId);
        if(indicator == null) {
            throw new ActionParamsException("No such indicator: " + indicatorId + " on datasource: " + pluginId);
        }
        try {
            JSONObject indicatorMetadata = toJSON(indicator);
            // Note that there is an another layer of caches in the plugins doing the web queries.
            // Two layers are necessary, because deserialization and conversion to the internal data model
            // is pretty heavy operation.
            if (plugin.canCache() && indicatorMetadata != null) {
                JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, indicatorMetadata.toString());
            }
            return indicatorMetadata;
        } catch (JSONException e) {
            throw new ActionException("Something went wrong in getting indicator metadata.", e);
        }
    }
    
    public JSONObject toJSON(StatisticalIndicator indicator) throws JSONException {
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
        return pluginIndicatorJSON;
    }

    public JSONArray toJSON(StatisticalIndicatorDataModel selectors) throws JSONException {
        JSONArray selectorsJSON = new JSONArray();
        for (StatisticalIndicatorDataDimension selector : selectors.getDimensions()) {
            JSONObject selectorJSON = new JSONObject();
            selectorJSON.put("id", selector.getId());
            selectorJSON.put("name", selector.getName());
            selectorJSON.put("allowedValues", toJSON(selector.getAllowedValues()));
            // Note: Values are not given here, they are null anyhow in this phase.
            selectorsJSON.put(selectorJSON);
        }
        return selectorsJSON;
    }

    private JSONArray toJSON(Collection<IdNamePair> stringCollection) {
        JSONArray stringArray = new JSONArray();
        for (IdNamePair value : stringCollection) {
            stringArray.put(value.getValueForJson());
        }
        return stringArray;
    }

    public JSONArray toJSON(List<StatisticalIndicatorLayer> layers) throws JSONException {
        JSONArray layersJSON = new JSONArray();
        for (StatisticalIndicatorLayer layer: layers) {
            layersJSON.put(layer.getOskariLayerId());
        }
        return layersJSON;
    }

}

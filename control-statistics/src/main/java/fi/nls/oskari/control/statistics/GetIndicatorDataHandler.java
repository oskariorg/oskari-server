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
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This ActionHandler retrieves data for an indicator for the frontend
 * 
 * Response is in JSON, and contains the indicator data.
 */
@OskariActionRoute("GetIndicatorData")
public class GetIndicatorDataHandler extends ActionHandler {

    private final static String PARAM_LAYER_ID = "regionset"; // previously layer_id
    private final static String PARAM_SELECTORS = "selectors";

    /**
     * For now, this uses pretty much static global store for the plugins.
     * In the future it might make sense to inject the pluginManager references to different controllers using DI.
     */
    private static final StatisticalDatasourcePluginManager PLUGIN_MANAGER = StatisticalDatasourcePluginManager.getInstance();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final long pluginId = params.getRequiredParamLong(StatisticsHelper.PARAM_DATASOURCE_ID);
        final String indicatorId = params.getRequiredParam(StatisticsHelper.PARAM_INDICATOR_ID);
        final long layerId = params.getRequiredParamLong(PARAM_LAYER_ID);
        final String selectors = params.getRequiredParam(PARAM_SELECTORS);
        JSONObject selectorsJSON;
        try {
            selectorsJSON = new JSONObject(selectors);
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid parameter value for key: "
                    + PARAM_SELECTORS + " - expected JSON object");
        }
        JSONObject response = getIndicatorDataJSON(params.getUser(),
                pluginId, indicatorId, layerId, selectorsJSON);
        ResponseHelper.writeResponse(params, response);
    }

    private JSONObject getIndicatorDataJSON(User user, long pluginId, String indicatorId,
            long layerId, JSONObject selectorJSON) throws ActionException {
        StatisticalDatasourcePlugin plugin = PLUGIN_MANAGER.getPlugin(pluginId);
        if (plugin == null) {
            throw new ActionParamsException("No such datasource: " + pluginId);
        }

        StatisticalIndicator indicator = plugin.getIndicator(user, indicatorId);
        if (indicator == null) {
            // indicator can be null if user doesn't have permission to it
            throw new ActionParamsException("No such indicator: " + indicatorId + " on datasource: " + pluginId);
        }

        String cacheKey = StatisticsHelper.getIndicatorDataCacheKey(pluginId, indicatorId, layerId, selectorJSON);
        if (plugin.canCache()) {
            JSONObject cached = getFromCache(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        StatisticalIndicatorLayer layer = indicator.getLayer(layerId);
        if (layer == null) {
            throw new ActionParamsException("No such regionset: " + layerId);
        }

        StatisticalIndicatorDataModel selectors = StatisticsHelper.getIndicatorDataModel(selectorJSON);
        try {
            Map<String, IndicatorValue> values = plugin.getIndicatorValues(indicator, selectors, layer);
            JSONObject response = toJSON(values);

            if (plugin.canCache()) {
                JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, response.toString());
            }

            return response;
        } catch (APIException e) {
            if (e.getCause() instanceof IOException) {
                throw new ActionParamsException(e.getMessage());
            } else {
                throw new ActionException(e.getMessage(), e);
            }
        }
    }

    private JSONObject getFromCache(String cacheKey) {
        String cachedData = JedisManager.get(cacheKey);
        return JSONHelper.createJSONObject(cachedData);
    }


    private JSONObject toJSON(Map<String, IndicatorValue> values) throws ActionException {
        try {
            JSONObject json = new JSONObject();
            for (Entry<String, IndicatorValue> entry : values.entrySet()) {
                entry.getValue().putToJSONObject(json, entry.getKey());
            }
            return json;
        } catch (JSONException e) {
            throw new ActionException("Something went wrong in serializing indicator data", e);
        }
    }
}

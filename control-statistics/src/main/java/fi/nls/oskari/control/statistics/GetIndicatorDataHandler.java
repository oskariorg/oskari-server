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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * This interface gives the data for one indicator to the frontend for showing it on the map and on the table.
 * 
 * - action_route=GetIndicatorData&plugin_id=plugin_id&indicator_id=indicator_id&layer_id=layer_id&selectors=URL_ENCODED_JSON
 * 
 * For example SotkaNET requires selectors for year and gender. This means that selectors parameter content
 * could be for example: selectors=%7B%22gender%22%3A%20%22male%22%2C%20%22year%22%3A%20%222005%22%7D
 * 
 * Response is in JSON, and contains the indicator data.
 */
@OskariActionRoute("GetIndicatorData")
public class GetIndicatorDataHandler extends ActionHandler {

    private final static String PARAM_PLUGIN_ID = "datasource"; // previously plugin_id
    private final static String PARAM_INDICATOR_ID = "indicator"; // previously indicator_id
    private final static String PARAM_LAYER_ID = "regionset"; // previously layer_id
    private final static String PARAM_SELECTORS = "selectors";

    /**
     * For now, this uses pretty much static global store for the plugins.
     * In the future it might make sense to inject the pluginManager references to different controllers using DI.
     */
    private static final StatisticalDatasourcePluginManager PLUGIN_MANAGER = StatisticalDatasourcePluginManager.getInstance();

    @Override
    public void handleAction(ActionParameters ap) throws ActionException {
        final long pluginId = ap.getRequiredParamLong(PARAM_PLUGIN_ID);
        final String indicatorId = ap.getRequiredParam(PARAM_INDICATOR_ID);
        final long layerId = ap.getRequiredParamLong(PARAM_LAYER_ID);
        final String selectors = ap.getRequiredParam(PARAM_SELECTORS);
        JSONObject selectorsJSON;
        try {
            selectorsJSON = new JSONObject(selectors);
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid parameter value "
                    + PARAM_SELECTORS + " expected JSON object");
        }
        JSONObject response = getIndicatorDataJSON(ap.getUser(), pluginId, indicatorId, layerId, selectorsJSON);
        ResponseHelper.writeResponse(ap, response);
    }

    public JSONObject getIndicatorDataJSON(User user, long pluginId, String indicatorId,
            long layerId, JSONObject selectorJSON) throws ActionException {
        final StatisticalDatasourcePlugin plugin = PLUGIN_MANAGER.getPlugin(pluginId);

        final String cacheKey = GetIndicatorDataHelper.getCacheKey(pluginId, indicatorId, layerId, selectorJSON);
        if (plugin.canCache()) {
            JSONObject cached = getFromCache(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        JSONObject response;
        try {

            // TODO: Might be faster to store the indicator id to indicator map in a proper map.
            //       Who should do this, though? We don't want to put this functionality into the plugins.
            //       It should be in a common wrapper for the plugins.

            StatisticalIndicator indicator = plugin.getIndicator(user, indicatorId);
            if(indicator == null) {
                throw new ActionParamsException("No such indicator");
            }

            StatisticalIndicatorLayer layer = indicator.getLayer(layerId);
            if(layer == null) {
                throw new ActionParamsException("No such regionset");
            }

            // Note: Layer version is handled already in the indicator metadata.
            // We found the correct indicator and the layer.

            StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
            @SuppressWarnings("unchecked")
            Iterator<String> keys = selectorJSON.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = selectorJSON.getString(key);
                StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(key, value);
                selectors.addDimension(selector);
            }
            Map<String, IndicatorValue> values = plugin.getIndicatorValues(indicator, selectors, layer);
            response = toJSON(values);
        } catch (Exception e) {
            if(e instanceof ActionException) {
                throw (ActionException)e;
            }
            throw new ActionException("Something went wrong in serializing indicator data.", e);
        }
        // Note that there is an another layer of caches in the plugins doing the web queries.
        // Two layers are necessary, because deserialization and conversion to the internal data model
        // is a pretty heavy operation.
        if (plugin.canCache() && response != null) {
            JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, response.toString());
        }
        return response;
    }

    private JSONObject getFromCache(String cacheKey) {
        String cachedData = JedisManager.get(cacheKey);
        return JSONHelper.createJSONObject(cachedData);
    }

    private JSONObject toJSON(Map<String, IndicatorValue> values) throws JSONException {
        JSONObject json = new JSONObject();
        for (Entry<String, IndicatorValue> entry : values.entrySet()) {
            entry.getValue().putToJSONObject(json, entry.getKey());
        }
        return json;
    }
}

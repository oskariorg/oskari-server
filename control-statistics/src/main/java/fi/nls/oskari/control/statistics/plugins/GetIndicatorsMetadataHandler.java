package fi.nls.oskari.control.statistics.plugins;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
 * This interface gives the relevant information for all the indicators to the frontend.
 * This information can be subsequently used to query the actual indicator data.
 * 
 * - action_route=GetIndicatorsMetadata
 * 
 * eg.
 * OSKARI_URL?action_route=GetIndicatorsMetadata
 * Response is in JSON, and contains the indicator metadata for each plugin separately.
 */
@OskariActionRoute("GetIndicatorsMetadata")
public class GetIndicatorsMetadataHandler extends ActionHandler {
    private final static String CACHE_PREFIX = "oskari_get_indicators_metadata_handler_";

    /**
     * For now, this uses pretty much static global store for the plugins.
     * In the future it might make sense to inject the pluginManager references to different controllers using DI.
     */
    private static final StatisticalDatasourcePluginManager pluginManager = new StatisticalDatasourcePluginManager();
    
    /**
     * For scheduling the cache refresh for the indicator lists.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void init() {
        pluginManager.init();

        // Refreshing the cache at boot, everything but the user indicators.
        // Refreshing in the background for every 8 hours also.
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    GetIndicatorsMetadataHandler.this.getIndicatorsMetadataJSON(null, true);
                } catch (ActionException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 8, TimeUnit.HOURS);
    }
    
    @Override
    public void handleAction(ActionParameters ap) throws ActionException {
        JSONObject response = getIndicatorsMetadataJSON(ap.getUser(), false);
        ResponseHelper.writeResponse(ap, response);
    }
    
    /**
     * Requests new data skipping the cache. Used for cache refresh before expiration.
     * @return
     * @throws ActionException
     */
    JSONObject getIndicatorsMetadataJSON(User user, boolean refreshCache) throws ActionException {
        JSONObject response = new JSONObject();
        Collection<StatisticalDatasourcePlugin> plugins = pluginManager.getPlugins();
        for (StatisticalDatasourcePlugin plugin : plugins) {
            String cacheKey = CACHE_PREFIX + plugin.getClass().getCanonicalName();
            if (plugin.canCache() && !refreshCache) {
                final String cachedData = JedisManager.get(cacheKey);
                if (cachedData != null && !cachedData.isEmpty()) {
                    try {
                        response.put(plugin.getClass().getCanonicalName(), new JSONObject(cachedData));
                        continue;
                    } catch (JSONException e) {
                        // Failed serializing. Skipping the cache.
                    }
                }
            }
            String localizationKey = pluginManager.getPluginLocalizationKey(plugin.getClass());
            JSONObject pluginMetadata = new JSONObject();
            try {
                pluginMetadata.put("localizationKey", localizationKey);
                JSONObject pluginIndicators = new JSONObject();
                for (StatisticalIndicator indicator : plugin.getIndicators(user)) {
                    JSONObject pluginIndicatorJSON = toJSON(indicator);
                    pluginIndicators.put(indicator.getId(), pluginIndicatorJSON);
                }
                pluginMetadata.put("indicators", pluginIndicators);
                response.put(plugin.getClass().getCanonicalName(), pluginMetadata);
                // Note that there is an another layer of caches in the plugins doing the web queries.
                // Two layers are necessary, because deserialization and conversion to the internal data model
                // is pretty heavy operation.
                if (plugin.canCache()) {
                    JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, pluginMetadata.toString());
                }
            } catch (JSONException e) {
                throw new ActionException("Something went wrong in getting indicator metadata.", e);
            }
        }
        return response;
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
            layerJSON.put("layerId", layer.getOskariLayerName());
            layersJSON.put(layerJSON);
        }
        return layersJSON;
    }

}

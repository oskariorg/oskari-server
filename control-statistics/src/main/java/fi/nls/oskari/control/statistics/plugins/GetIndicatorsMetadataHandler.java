package fi.nls.oskari.control.statistics.plugins;

import java.util.Map;

import fi.nls.oskari.domain.map.JSONLocalized;
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
 * This information can be subsequently used to query the indicator selector metadata.
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

    @Override
    public void init() {
        pluginManager.init();
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
        Map<Long, StatisticalDatasourcePlugin> plugins = pluginManager.getPlugins();
        for (Long id : plugins.keySet()) {
            StatisticalDatasourcePlugin plugin = plugins.get(id);
            String cacheKey = CACHE_PREFIX + id;
            if (plugin.canCache() && !refreshCache) {
                final String cachedData = JedisManager.get(cacheKey);
                if (cachedData != null && !cachedData.isEmpty()) {
                    try {
                        response.put(Long.toString(id), new JSONObject(cachedData));
                        continue;
                    } catch (JSONException e) {
                        // Failed serializing. Skipping the cache.
                    }
                }
            }
            JSONLocalized locale = pluginManager.getPluginLocale(id);
            JSONObject pluginMetadata = new JSONObject();
            try {
                // TODO: maybe just list the current language?
                pluginMetadata.put("locale", locale.getLocale());
                JSONObject pluginIndicators = new JSONObject();
                for (StatisticalIndicator indicator : plugin.getIndicators(user)) {
                    JSONObject pluginIndicatorJSON = toJSON(indicator);
                    pluginIndicators.put(indicator.getId(), pluginIndicatorJSON);
                }
                pluginMetadata.put("indicators", pluginIndicators);
                response.put(Long.toString(id), pluginMetadata);
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
        Map<String, String> source = indicator.getLocalizedSource();
        
        pluginIndicatorJSON.put("name", name);
        pluginIndicatorJSON.put("source", source);
        pluginIndicatorJSON.put("public", indicator.isPublic());
        return pluginIndicatorJSON;
    }
}

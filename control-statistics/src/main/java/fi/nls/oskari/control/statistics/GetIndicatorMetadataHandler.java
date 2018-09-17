package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

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

    /**
     * For now, this uses pretty much static global store for the plugins.
     * In the future it might make sense to inject the pluginManager references to different controllers using DI.
     */
    private static final StatisticalDatasourcePluginManager pluginManager = StatisticalDatasourcePluginManager.getInstance();

    @Override
    public void handleAction(ActionParameters ap) throws ActionException {
        final long pluginId = ap.getRequiredParamInt(StatisticsHelper.PARAM_DATASOURCE_ID);
        final String indicatorId = ap.getRequiredParam(StatisticsHelper.PARAM_INDICATOR_ID);
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
        StatisticalIndicator indicator = plugin.getIndicator(user, indicatorId);
        if(indicator == null) {
            // indicator can be null if user doesn't have permission to it
            throw new ActionParamsException("No such indicator: " + indicatorId + " on datasource: " + pluginId);
        }
        String cacheKey = StatisticsHelper.getIndicatorMetadataCacheKey(pluginId, indicatorId);
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
            JSONObject indicatorMetadata = StatisticsHelper.toJSON(indicator);
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

}

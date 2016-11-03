package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePluginManager;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static fi.nls.oskari.control.ActionConstants.*;

import java.util.Map;

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
@OskariActionRoute("GetIndicatorList")
public class GetIndicatorListHandler extends ActionHandler {
    private final static String CACHE_PREFIX = "oskari_sotka_indicatorlist:";

    private static final String KEY_INDICATORS = "indicators";
    private static final String PARAM_DATASOURCE = "datasource";
    /**
     * For now, this uses pretty much static global store for the plugins.
     * In the future it might make sense to inject the pluginManager references to different controllers using DI.
     */
    private static final StatisticalDatasourcePluginManager pluginManager = StatisticalDatasourcePluginManager.getInstance();

    @Override
    public void handleAction(ActionParameters ap) throws ActionException {
        final int srcId = ap.getRequiredParamInt(PARAM_DATASOURCE);
        JSONObject response = getIndicatorsListJSON(srcId, ap.getUser());
        ResponseHelper.writeResponse(ap, response);
    }

    /**
     * Requests new data skipping the cache. Used for cache refresh before expiration.
     * @return
     * @throws ActionException
     */
    JSONObject getIndicatorsListJSON(long datasourceId, User user) throws ActionException {
        String cacheKey = CACHE_PREFIX + datasourceId + ":" + user.getId();
        final StatisticalDatasourcePlugin plugin = pluginManager.getPlugin(datasourceId);
        if(plugin == null) {
            throw new ActionParamsException("No such datasource (id=" + datasourceId + ").");
        }

        // try cache
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

        JSONObject response = new JSONObject();
        //plugin.getIndicators(user);
        final JSONArray indicators = new JSONArray();
        JSONHelper.putValue(response, KEY_INDICATORS, indicators);
        for (StatisticalIndicator indicator : plugin.getIndicators(user)) {
            if(indicator.getLayers() != null && indicator.getLayers().size()>0) {
                indicators.put(toJSON(indicator));
            }
        }
        // Note that there is an another layer of caches in the plugins doing the web queries.
        // Two layers are necessary, because deserialization and conversion to the internal data model
        // is pretty heavy operation.
        if (plugin.canCache()) {
            JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, response.toString());
        }
        return response;
    }

    private JSONObject toJSON(StatisticalIndicator indicator) {
        final JSONObject json = new JSONObject();
        JSONHelper.putValue(json, KEY_ID, indicator.getId());
        final Map<String, String> name = indicator.getLocalizedName();
        JSONHelper.putValue(json, KEY_NAME, name);
        return json;
    }
}

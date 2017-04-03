package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.control.statistics.xml.Region;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static fi.nls.oskari.control.ActionConstants.*;

import java.io.IOException;
import java.util.List;

/**
 * Returns the region information.
 * Sample response:
 * {
     "regions" : [{
         "id" : 091,
         "name" : "Lappeenranta"
     }, ...]
 */
@OskariActionRoute("GetRegions")
public class GetRegionsHandler extends ActionHandler {
    private final static String CACHE_KEY_PREFIX = "oskari_get_layer_regions_handler:";

    private static final String KEY_REGIONS = "regions";

    private RegionSetService service;


    public void setRegionsetService(final RegionSetService service) {
        this.service = service;
    }

    @Override
    public void init() {
        if(service == null) {
            setRegionsetService(OskariComponentManager.getComponentOfType(RegionSetService.class));
        }
    }

    public void handleAction(ActionParameters ap) throws ActionException {
        final int layerId = ap.getRequiredParamInt("regionset");
        JSONObject response = getRegionInfoJSON(layerId);
        ResponseHelper.writeResponse(ap, response);
    }

    /**
     * 
     * @param layerId For example: 9
     * @return For example: [{"name": "Alajärvi"}]
     * @throws ActionException
     */
    public JSONObject getRegionInfoJSON(long layerId) throws ActionException {
        final RegionSet regionset = service.getRegionSet(layerId);
        
        if (regionset == null) {
            throw new ActionParamsException("Regionset not found");
        }
        return requestRegionInfoJSON(layerId, regionset);
    }

    public JSONObject requestRegionInfoJSON(long id, RegionSet regionset) throws ActionException {
        final String cacheKey = CACHE_KEY_PREFIX + id;
        final String cachedData = JedisManager.get(cacheKey);
        if (cachedData != null && !cachedData.isEmpty()) {
            try {
                return new JSONObject(cachedData);
            } catch (JSONException e) {
                // Failed serializing. Skipping the cache.
            }
        }
        final JSONObject response = new JSONObject();
        final JSONArray regions = new JSONArray();
        JSONHelper.putValue(response, KEY_REGIONS, regions);

        try {
            final List<Region> result = service.getRegions(regionset);
            for (Region codeName : result) {
                JSONObject item = new JSONObject();
                JSONHelper.putValue(item, KEY_ID, codeName.getCode());
                JSONHelper.putValue(item, KEY_NAME, codeName.getName());
                regions.put(item);
            }
        } catch (IOException e) {
            throw new ActionException("Something went wrong fetching the region info from geoserver.", e);
        }

        JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, response.toString());
        return response;
    }
}

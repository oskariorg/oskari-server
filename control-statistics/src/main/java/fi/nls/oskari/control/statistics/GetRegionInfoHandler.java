package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.control.statistics.xml.RegionCodeNamePair;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Returns the region information.
 * Sample response:
 * {
 * "005": {
 *   "name": "Ikaalinen"
 * }
 */
@OskariActionRoute("GetRegionInfo")
public class GetRegionInfoHandler extends ActionHandler {
    private final static String CACHE_KEY_PREFIX = "oskari_get_layer_info_handler:";

    private RegionSetService service;
    
    public void handleAction(ActionParameters ap) throws ActionException {
        final String layerId = ap.getRequiredParam("layer_id");
        final String regionCode = ap.getHttpParam("region_id");
        JSONObject response = getRegionInfoJSON(Long.valueOf(layerId), regionCode);
        ResponseHelper.writeResponse(ap, response);
    }

    /**
     * 
     * @param layerId For example: 9
     * @param regionCode: For example: "005", or null
     * @return For example: [{"name": "Alajärvi"}]
     * @throws ActionException
     */
    public JSONObject getRegionInfoJSON(long layerId, String regionCode) throws ActionException {
        final RegionSet regionset = service.getRegionSet(layerId);
        
        if (regionset == null) {
            throw new ActionParamsException("Regionset not found");
        }
        return requestRegionInfoJSON(regionCode, layerId, regionset);
    }

    public JSONObject requestRegionInfoJSON(String regionCode, long id, RegionSet regionset) throws ActionException {
        final String cacheKey = CACHE_KEY_PREFIX + id + ":" + regionCode;
        final String cachedData = JedisManager.get(cacheKey);
        if (cachedData != null && !cachedData.isEmpty()) {
            try {
                return new JSONObject(cachedData);
            } catch (JSONException e) {
                // Failed serializing. Skipping the cache.
            }
        }
        final JSONObject response = new JSONObject();

        try {
            List<RegionCodeNamePair> result = service.getRegions(regionset);
            for (RegionCodeNamePair codeName : result) {
                if (regionCode != null && !codeName.getCode().equals(regionCode)) {
                    continue;
                }
                JSONObject regionJSON = JSONHelper.createJSONObject("name", codeName.getName());
                response.put(codeName.getCode(), regionJSON);
            }
        } catch (IOException e) {
            throw new ActionException("Something went wrong fetching the region info from geoserver.", e);
        } catch (JSONException e) {
            throw new ActionException("Something went wrong serializing the region info response.", e);
        }

        JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, response.toString());
        return response;
    }

    public void setRegionsetService(final RegionSetService service) {
        this.service = service;
    }

    @Override
    public void init() {
        if(service == null) {
            setRegionsetService(OskariComponentManager.getComponentOfType(RegionSetService.class));
        }
    }
}

package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;
import java.util.List;

/**
 * Returns the region information.
 * Sample response:
 * {
     "regions" : [{
         "id" : 091,
         "name" : "Lappeenranta",
         "point" : {
            "lon" : <x>,
            "lat" : <y>
         },
         "geojson" : { ... }
     }, ...]
 */
@OskariActionRoute("GetRegions")
public class GetRegionsHandler extends ActionHandler {

    private static final String CACHE_KEY_PREFIX = "oskari:stats:regionset:";
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
        final String srs = ap.getRequiredParam(ActionConstants.PARAM_SRS);
        JSONObject response = getRegionInfoJSON(layerId, srs);
        ResponseHelper.writeResponse(ap, response);
    }

    /**
     * @param layerId For example: 9
     * @return For example: [{"name": "Alajärvi"}]
     * @throws ActionException
     */
    public JSONObject getRegionInfoJSON(long layerId, final String srs) throws ActionException {
        final RegionSet regionset = service.getRegionSet(layerId);

        if (regionset == null) {
            throw new ActionParamsException("Regionset not found");
        }
        return requestRegionInfoJSON(layerId, regionset, srs);
    }

    public JSONObject requestRegionInfoJSON(long id, RegionSet regionset, final String srs) throws ActionException {
        final String cacheKey = CACHE_KEY_PREFIX + id + ":" + srs;
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
            final List<Region> result = RegionSetHelper.getRegions(regionset, srs);
            for (Region region : result) {
                regions.put(region.toJSON());
            }
        } catch (IOException e) {
            throw new ActionException("Couldn't connect to regionset provider.", e);
        } catch (ServiceException e) {
            throw new ActionException("Regionset provider misconfiguration.", e);
        } catch (ServiceRuntimeException e) {
            throw new ActionException("Regionset provider returned unexpected response.", e);
        } catch (FactoryException | MismatchedDimensionException e) {
            throw new ActionException("Failed to create transformation", e);
        } catch (TransformException e) {
            throw new ActionException("Failed to perform transformation", e);
        }

        JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, response.toString());
        return response;
    }

}

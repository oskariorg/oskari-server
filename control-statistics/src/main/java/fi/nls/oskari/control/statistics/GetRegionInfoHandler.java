package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.statistics.db.LayerMetadata;
import fi.nls.oskari.control.statistics.xml.RegionCodeNamePair;
import fi.nls.oskari.control.statistics.xml.WfsXmlParser;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

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

    // TODO: With DI, this could be injected. Here, we just make an another instance.
    private GetLayerInfoHandler layerInfoHandler = new GetLayerInfoHandler();
    
    public void handleAction(ActionParameters ap) throws ActionException {
        final String layerId = ap.getRequiredParam("layer_id");
        final String regionCode = ap.getHttpParam("region_id");
        JSONObject response = getRegionInfoJSON(Long.valueOf(layerId), regionCode);
        ResponseHelper.writeResponse(ap, response);
    }

    /**
     * 
     * @param layerId For example: 9
     * @param regionId: For example: "005", or null
     * @return For example: [{"name": "Alajärvi"}]
     * @throws ActionException
     */
    public JSONObject getRegionInfoJSON(long layerId, String regionCode) throws ActionException {
        final LayerMetadata layerMetadata = layerInfoHandler.getLayerMetadata().get(layerId);
        
        if (layerMetadata == null) {
            return new JSONObject();
        }
        try {
            final JSONObject attributes = new JSONObject(layerMetadata.getAttributes());
            final JSONObject statistics = attributes.getJSONObject("statistics");

            final String nameTag = statistics.getString("nameIdTag");
            final String idTag = statistics.getString("regionIdTag");
            final String name = layerMetadata.getOskariLayerName();
            final String urlBase = layerMetadata.getUrl();
            final String featuresUrl = statistics.getString("featuresUrl");
            final JSONObject response = requestRegionInfoJSON(regionCode, layerId, name, nameTag, idTag, urlBase, featuresUrl);
            return response;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new ActionException("Something went wrong creating RegionInfoJSON.", e);
        }
    }

    public JSONObject requestRegionInfoJSON(String regionCode, long id, String name, String nameTag, String idTag,
            String urlBase, String featuresUrl) throws ActionException {
        final String cacheKey = CACHE_KEY_PREFIX + id + ":" + regionCode;
        final String cachedData = JedisManager.get(cacheKey);
        if (cachedData != null && !cachedData.isEmpty()) {
            try {
                return new JSONObject(cachedData);
            } catch (JSONException e) {
                // Failed serializing. Skipping the cache.
            }
        }
        
        // For example: http://localhost:8080/geoserver/wfs?service=wfs&version=2.0.0&request=GetFeature&typeNames=oskari:kunnat2013&propertyName=kuntakoodi,kuntanimi
        String url = featuresUrl + "?service=wfs&version=2.0.0&request=GetFeature&typeNames=" + name +
                "&propertyName=" + idTag + "," + nameTag;
        final JSONObject response = new JSONObject();

        try {
            String xmlGeoserverResponseWithAllRegions = IOHelper.getURL(url);
            List<RegionCodeNamePair> result = WfsXmlParser.parse(xmlGeoserverResponseWithAllRegions, idTag, nameTag);
            for (RegionCodeNamePair codeName : result) {
                if (regionCode == null || codeName.getCode().equals(regionCode)) {
                    JSONObject regionJSON = new JSONObject();
                    regionJSON.put("name", codeName.getName());
                    response.put(codeName.getCode(), regionJSON);
                }
            }
        } catch (IOException e) {
            throw new ActionException("Something went wrong fetching the region info from the geoserver.", e);
        } catch (XMLStreamException e) {
            throw new ActionException("Something went wrong parsing the region info from the geoserver.", e);
        } catch (JSONException e) {
            throw new ActionException("Something went wrong serializing the region info response.", e);
        }

        JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, response.toString());
        return response;
    }

    @Override
    public void init() {
        this.layerInfoHandler.init();
    }
}

package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.statistics.db.Layer;
import fi.nls.oskari.control.statistics.xml.RegionCodeNamePair;
import fi.nls.oskari.control.statistics.xml.WfsXmlParser;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final static String CACHE_KEY_REGION_XML = "oskari_get_layer_info_region_xml";

    // TODO: With DI, this could be injected. Here, we just make an another instance.
    private GetLayerInfoHandler layerInfoHandler = new GetLayerInfoHandler();
    private Map<String, Layer> layerInfoMap;
    
    public void handleAction(ActionParameters ap) throws ActionException {
        final String layerId = ap.getRequiredParam("layer_id");
        final String regionCode = ap.getHttpParam("region_id");
        JSONObject response = getRegionInfoJSON(layerId, regionCode);
        ResponseHelper.writeResponse(ap, response);
    }

    /**
     * 
     * @param layerId For example: "oskari:kunnat2013"
     * @param regionId: For example: "005", or null
     * @return For example: [{"name": "Alajärvi"}]
     * @throws ActionException
     */
    public JSONObject getRegionInfoJSON(String layerId, String regionCode) throws ActionException {
        final Layer layerInfo = this.layerInfoMap.get(layerId);
        if (layerInfo == null) {
            return new JSONObject();
        }
        final String name = layerInfo.getOskariLayerName();
        final String nameTag = layerInfo.getOskariNameIdTag();
        final String idTag = layerInfo.getOskariRegionIdTag();
        final String urlBase = layerInfoHandler.getLayerMetadata().get(name).getUrl();
        JSONObject response = requestRegionInfoJSON(regionCode, name, nameTag, idTag, urlBase);
        return response;
    }

    public JSONObject requestRegionInfoJSON(String regionCode, String name, String nameTag, String idTag, String urlBase) throws ActionException {
        final String cacheKey = CACHE_KEY_PREFIX + name + ":" + regionCode;
        final String cachedData = JedisManager.get(cacheKey);
        if (cachedData != null && !cachedData.isEmpty()) {
            try {
                return new JSONObject(cachedData);
            } catch (JSONException e) {
                // Failed serializing. Skipping the cache.
            }
        }
        
        // For example: http://localhost:8080/geoserver/wfs?service=wfs&version=2.0.0&request=GetFeature&typeNames=oskari:kunnat2013&propertyName=kuntakoodi,kuntanimi
        String url = urlBase + "/wfs?service=wfs&version=2.0.0&request=GetFeature&typeNames=" + name +
                "&propertyName=" + idTag + "," + nameTag;
        final JSONObject response = new JSONObject();
        
        String xmlGeoserverResponseWithAllRegions = JedisManager.get(CACHE_KEY_REGION_XML);
        if (xmlGeoserverResponseWithAllRegions == null) {
            try {
                xmlGeoserverResponseWithAllRegions = IOHelper.getURL(url);
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
            JedisManager.setex(CACHE_KEY_REGION_XML, JedisManager.EXPIRY_TIME_DAY, xmlGeoserverResponseWithAllRegions);
        }
        
        JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, response.toString());
        return response;
    }

    @Override
    public void init() {
        this.layerInfoHandler.init();
        this.layerInfoMap = new HashMap<>();
        for (Layer layer : this.layerInfoHandler.getLayers()) {
            this.layerInfoMap.put(layer.getOskariLayerName(), layer);
        }
    }
}

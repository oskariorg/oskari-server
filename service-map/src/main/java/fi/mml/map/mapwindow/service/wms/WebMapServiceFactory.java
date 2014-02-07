package fi.mml.map.mapwindow.service.wms;

import fi.mml.map.mapwindow.service.db.CapabilitiesCacheService;
import fi.mml.map.mapwindow.service.db.CapabilitiesCacheServiceIbatisImpl;
import fi.mml.map.mapwindow.util.RemoteServiceDownException;
import fi.nls.oskari.domain.map.CapabilitiesCache;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating WMS objects
 * 
 */
public class WebMapServiceFactory {

	/** Logger */
	private static Logger log = LogFactory.getLogger(WebMapServiceFactory.class);
    private static final CapabilitiesCacheService capabilitiesCacheService = new CapabilitiesCacheServiceIbatisImpl();
    static long wmsCachedtime = 0;
    static long wmsExpirationTime = 12*60*60*1000;
    static Map<String, WebMapService> wmsCache = new HashMap<String, WebMapService>();


	
	/**
	 * Builds new WMS interface with correct version
	 * 
	 * @param layerId id of the map layer
	 * @param layerName name of the map layer
	 * 
	 * @return WebMapService implementation that service url is implemented
	 * @throws WebMapServiceParseException if something goes wrong when parsing
	 * @throws RemoteServiceDownException if Web Map service is down
	 */
	public static WebMapService buildWebMapService(int layerId, String layerName) throws WebMapServiceParseException {

		if (wmsCachedtime + wmsExpirationTime < System.currentTimeMillis()) {
            flushCache();
		}
		WebMapService wms = null;
        // caching since this is called whenever a layer JSON is created!!
		if (wmsCache.containsKey("wmsCache_"+layerId)) {
			wms = wmsCache.get("wmsCache_"+layerId);
		} else {
            CapabilitiesCache cc = capabilitiesCacheService.find(layerId);
            if (cc != null && "1.3.0".equals(cc.getVersion().trim())) {
                wms = new WebMapServiceV1_3_0_Impl("from DataBase", cc.getData().trim(), layerName);
            } else if (cc != null && "1.1.1".equals(cc.getVersion().trim())) {
                wms = new WebMapServiceV1_1_1_Impl("from DataBase", cc.getData().trim(), layerName);
            }
			wmsCache.put("wmsCache_"+layerId, wms);
		}

		
		return wms;
	}

    public static void flushCache(final int layerId) {
        wmsCache.remove("wmsCache_"+layerId);
    }

    public static void flushCache() {
        wmsCache = new HashMap<String, WebMapService>();
        wmsCachedtime = System.currentTimeMillis();
    }
	
	
	/**
	 * We are supporting a feature that user can give multiple WMS urls separated by comma.
	 * In here we check if two or more urls are actually given and return only the first one
	 * since they should be indentical 
	 * 
	 * @param url
	 * @return
	 */
	private static String parseOnlyOneWMSUrl(String url) {
		if (url.contains(",")) {
			return url.split(",")[0];
		} else {
			return url;
		}
	}
	
	/**
	 * Constructs a getCapabilities url 1.1.1 implementation from base url
	 * 
	 * @param url
	 * @return
	 */
	private static String buildGetCapabilitiesUrl_1_1_1(String url) {
		String finalUrl = url;
		if (finalUrl.substring(url.length()-1).equals("?")) {
			finalUrl += "VERSION=1.1.1&SERVICE=WMS&REQUEST=GetCapabilities";
		} else {
			finalUrl += "?VERSION=1.1.1&SERVICE=WMS&REQUEST=GetCapabilities";
		}
		
		return finalUrl;
	}
	
	/**
	 * Constructs a getCapabilities url 1.3.0 implementation from base url
	 * 
	 * @param url
	 * @return
	 */
	private static String buildGetCapabilitiesUrl_1_3_0(String url) {
		String finalUrl = url;
		if (finalUrl.substring(url.length()-1).equals("?")) {
			finalUrl += "&SERVICE=WMS&REQUEST=GetCapabilities";
		} else {
			finalUrl += "?&SERVICE=WMS&REQUEST=GetCapabilities";
		}
		
		return finalUrl;
	}

	/**
	 * Returns true is data represents a WMS 1.1.1 version
	 * 
	 * @param data
	 * @return
	 */
	public static boolean isVersion1_1_1(String data) {
		if (data.indexOf("WMT_MS_Capabilities version=\"1.1.1\"") > 0) {
			return true;
		} else {
            return data.indexOf("WMT_MS_Capabilities updateSequence=\"1\" version=\"1.1.1\"") > 0;
        }
	}

	/**
	 * Returns true is data represents a WMS 1.3.0 version
	 * 
	 * @param data
	 * @return
	 */
	public static boolean isVersion1_3_0(String data) {
		return data.indexOf("WMS_Capabilities version=\"1.3.0\"") > 0;
	}



}

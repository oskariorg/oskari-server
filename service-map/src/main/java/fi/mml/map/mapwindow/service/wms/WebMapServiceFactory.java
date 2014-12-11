package fi.mml.map.mapwindow.service.wms;

import fi.mml.map.mapwindow.service.db.CapabilitiesCacheService;
import fi.mml.map.mapwindow.service.db.CapabilitiesCacheServiceIbatisImpl;
import fi.mml.map.mapwindow.util.RemoteServiceDownException;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.map.CapabilitiesCache;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.wms.WMSCapabilities;

/**
 * Factory for creating WMS objects
 * 
 */
public class WebMapServiceFactory {

	/** Logger */
	private static Logger log = LogFactory.getLogger(WebMapServiceFactory.class);
    private static final CapabilitiesCacheService capabilitiesCacheService = new CapabilitiesCacheServiceIbatisImpl();
    private static Cache<WebMapService> wmsCache = CacheManager.getCache(WebMapServiceFactory.class.getName());
    static {
        wmsCache.setExpiration(12L*60L*60L*1000L);
    }
	
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
        final String cacheKey = "wmsCache_" + layerId;
		WebMapService wms = wmsCache.get(cacheKey);
        // caching since this is called whenever a layer JSON is created!!
		if (wms == null) {
            CapabilitiesCache cc = capabilitiesCacheService.find(layerId);
            if(cc == null) {
                // setup empty capabilities so we don't try to parse again before cache flush
                WMSCapabilities emptyCaps = new WMSCapabilities();
                wmsCache.put(cacheKey, emptyCaps);
                return emptyCaps;
            }
            try {
                if (cc != null && "1.3.0".equals(cc.getVersion().trim())) {
                    wms = new WebMapServiceV1_3_0_Impl("from DataBase", cc.getData().trim(), layerName);
                } else if (cc != null && "1.1.1".equals(cc.getVersion().trim())) {
                    wms = new WebMapServiceV1_1_1_Impl("from DataBase", cc.getData().trim(), layerName);
                }
                // cache the parsed value
                wmsCache.put(cacheKey, wms);
            } catch (WebMapServiceParseException ex) {
                // setup empty capabilities so we don't try to parse again before cache flush
                wmsCache.put(cacheKey, new WMSCapabilities());
                throw ex;
            }
		}
		return wms;
	}

    public static void flushCache(final int layerId) {
        wmsCache.remove("wmsCache_"+layerId);
    }

    public static void flushCache() {
        wmsCache.flush(true);
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

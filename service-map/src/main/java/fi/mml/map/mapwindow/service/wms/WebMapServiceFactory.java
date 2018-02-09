package fi.mml.map.mapwindow.service.wms;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.wms.WMSCapabilities;

/**
 * Factory for creating WMS objects
 * 
 */
public class WebMapServiceFactory {

    private static final CapabilitiesCacheService CAPABILITIES_SERVICE = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);
    private static final OskariLayerService LAYER_SERVICE = new OskariLayerServiceIbatisImpl();
    private static Cache<WebMapService> wmsCache = CacheManager.getCache(WebMapServiceFactory.class.getName());
    static {
        wmsCache.setExpiration(12L*60L*60L*1000L);
    }
	
	/**
	 * Builds new WMS interface with correct version
	 * 
	 * @param layerId id of the map layer
	 * 
	 * @throws WebMapServiceParseException if something goes wrong when parsing
	 * @throws LayerNotFoundInCapabilitiesException if layer is not found in capabilities
	 */
	public static WebMapService buildWebMapService(int layerId)
	        throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        return buildWebMapService(LAYER_SERVICE.find(layerId));
    }

    public static WebMapService buildWebMapService(OskariLayer layer)
            throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        final String cacheKey = "wmsCache_" + layer.getId();
		WebMapService wms = wmsCache.get(cacheKey);
        // caching since this is called whenever a layer JSON is created!!
		if (wms == null) {
            OskariLayerCapabilities cc = getCaps(layer);
            if(cc == null) {
                // setup empty capabilities so we don't try to parse again before cache flush
                WMSCapabilities emptyCaps = new WMSCapabilities();
                wmsCache.put(cacheKey, emptyCaps);
                return emptyCaps;
            }
            try {
                final String data = cc.getData().trim();
                if (isVersion1_3_0(data)) {
                    wms = new WebMapServiceV1_3_0_Impl("from DataBase", data, layer.getName());
                } else if (isVersion1_1_1(data)) {
                    wms = new WebMapServiceV1_1_1_Impl("from DataBase", data, layer.getName());
                }
                if(wms != null) {
                    // cache the parsed value
                    wmsCache.put(cacheKey, wms);
                }
            } catch (WebMapServiceParseException ex) {
                // setup empty capabilities so we don't try to parse again before cache flush
                wmsCache.put(cacheKey, new WMSCapabilities());
                throw ex;
            }
		}
		return wms;
	}

    public static WebMapService createFromXML(final String layerName, final String xml)
            throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        if (isVersion1_3_0(xml)) {
            return new WebMapServiceV1_3_0_Impl("from DataBase", xml, layerName);
        } else if (isVersion1_1_1(xml)) {
            return new WebMapServiceV1_1_1_Impl("from DataBase", xml, layerName);
        } else {
            throw new WebMapServiceParseException("Could not detect version to be 1.3.0 or 1.1.1");
        }
    }

    private static OskariLayerCapabilities getCaps(OskariLayer layer) throws WebMapServiceParseException {
        try {
            return CAPABILITIES_SERVICE.getCapabilities(layer);
        } catch (Exception ex) {
            throw new WebMapServiceParseException(ex);
        }
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
        return data != null &&
                data.contains("WMT_MS_Capabilities") &&
                data.contains("version=\"1.1.1\"");
	}

	/**
	 * Returns true is data represents a WMS 1.3.0 version
	 * 
	 * @param data
	 * @return
	 */
	public static boolean isVersion1_3_0(String data) {
        return data != null &&
                data.contains("WMS_Capabilities") &&
                data.contains("version=\"1.3.0\"");
	}

}

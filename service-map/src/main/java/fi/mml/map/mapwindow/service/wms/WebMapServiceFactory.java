package fi.mml.map.mapwindow.service.wms;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.wms.WMSCapabilities;

/**
 * Factory for creating WMS objects
 * 
 */
@Deprecated
public class WebMapServiceFactory {

    private static final CapabilitiesCacheService CAPABILITIES_SERVICE = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);
    private static final OskariLayerService LAYER_SERVICE = new OskariLayerServiceMybatisImpl();
    private static Cache<WebMapService> wmsCache = CacheManager.getCache(WebMapServiceFactory.class.getName());
    static {
        wmsCache.setExpiration(12L*60L*60L*1000L);
    }
	
	/**
	 * Builds new WMS interface with correct version
	 * 
	 * @param layerId id of the map layer
	 * 
	 * @throws ServiceException if something goes wrong when getting capabilities from cache or from service 
	 * @throws WebMapServiceParseException if something goes wrong when parsing
	 * @throws LayerNotFoundInCapabilitiesException if layer is not found in capabilities
	 */
	@Deprecated
	public static WebMapService buildWebMapService(int layerId)
	        throws ServiceException, WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        return buildWebMapService(LAYER_SERVICE.find(layerId));
    }
	@Deprecated
	public static WebMapService buildWebMapService(OskariLayer layer)
	        throws ServiceException, WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
	    final String cacheKey = "wmsCache_" + layer.getId();

	    // Check own Cache<WebMapService>
	    WebMapService wms = wmsCache.get(cacheKey);
	    if (wms != null) {
	        return wms;
	    }

	    // Get Capabilities XML document from CapabilitiesCacheService
	    OskariLayerCapabilities cc = CAPABILITIES_SERVICE.getCapabilities(layer);
	    String data = cc.getData();

	    try {
	        wms = WebMapServiceFactory.createFromXML(layer.getName(), data);
	    } catch (WebMapServiceParseException | LayerNotFoundInCapabilitiesException ex) {
	        // setup empty capabilities so we don't try to parse again before cache flush
	        wmsCache.put(cacheKey, new WMSCapabilities());
	        throw ex;
	    }

	    if (cc.getId() == null) {
	        // Capabilities originated from the service and was parseable, save it to DB
	        CAPABILITIES_SERVICE.save(layer, cc.getData());
	    }

	    // Cache successfully parsed WebMapService
	    wmsCache.put(cacheKey, wms);

	    return wms;
	}

    @Deprecated
    public static WebMapService createFromXML(final String layerName, final String xml) throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        return WebMapServiceFactoryHelper.createFromXML(layerName, xml);
    }
	@Deprecated
    public static void flushCache(final int layerId) {
        wmsCache.remove("wmsCache_"+layerId);
    }
	@Deprecated
    public static void flushCache() {
        wmsCache.flush(true);
    }

    /**
     * Returns true if data represents a WMS 1.1.1 version
     * @deprecated use WebMapServiceFactoryHelper.isVersion1_1_1(String)
     */
    @Deprecated
    public static boolean isVersion1_1_1(String data) {
        return WebMapServiceFactoryHelper.isVersion1_1_1(data);
    }

    /**
     * Returns true if data represents a WMS 1.3.0 version
     * @deprecated use WebMapServiceFactoryHelper.isVersion1_3_0(String)
     */
    @Deprecated
    public static boolean isVersion1_3_0(String data) {
        return WebMapServiceFactoryHelper.isVersion1_3_0(data);
    }
}

package org.oskari.print.wmts;

import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import org.oskari.capabilities.ogc.wmts.WMTSCapabilities;
import org.oskari.capabilities.ogc.wmts.WMTSCapabilitiesParserHelper;
import org.oskari.print.request.PrintLayer;

/**
 * Caches parsed WMTSCapabilities
 */
public class WMTSCapabilitiesCache {

    private final CapabilitiesCacheService capabilitiesService;
    private final Cache<WMTSCapabilities> wmtsCapabilitiesCache;

    public WMTSCapabilitiesCache() {
        this(new CapabilitiesCacheServiceMybatisImpl());
    }

    public WMTSCapabilitiesCache(CapabilitiesCacheService capabilitiesService) {
        this.capabilitiesService = capabilitiesService;
        this.wmtsCapabilitiesCache = CacheManager.getCache(WMTSCapabilitiesCache.class.getName());
    }

    public WMTSCapabilities get(PrintLayer layer) throws ServiceException {
        WMTSCapabilities capabilities = wmtsCapabilitiesCache.get(Integer.toString(layer.getId()));
        if (capabilities == null) {
            capabilities = parseCapabilities(layer);
            wmtsCapabilitiesCache.put(Integer.toString(layer.getId()), capabilities);
        }
        return capabilities;
    }

    private WMTSCapabilities parseCapabilities(PrintLayer layer)
            throws ServiceException {
        try {
            final String url = layer.getUrl();
            final String type = layer.getType();
            final String version = layer.getVersion();
            final String user = layer.getUsername();
            final String pass = layer.getPassword();
            final OskariLayerCapabilities caps = capabilitiesService.getCapabilities(url, type, version, user, pass);
            final String data = caps.getData();
            WMTSCapabilities wmtsCaps = WMTSCapabilitiesParserHelper.parseCapabilities(data);
            if (caps.getId() == null) {
                capabilitiesService.save(caps);
            }
            return wmtsCaps;
        } catch (Exception e) {
            throw new ServiceException("Failed to parse WMTS capabilities, layerId: "
                    + layer.getId(), e);
        }
    }

}

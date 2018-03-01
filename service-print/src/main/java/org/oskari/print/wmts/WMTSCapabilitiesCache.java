package org.oskari.print.wmts;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
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
        this.wmtsCapabilitiesCache = new Cache<>();
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
            OskariLayerCapabilities xml = capabilitiesService.getCapabilities(layer.getUrl(),
                    layer.getType(), layer.getUsername(), layer.getPassword(), layer.getVersion());
            return WMTSCapabilitiesParser.parseCapabilities(xml.getData());
        } catch (Exception e) {
            throw new ServiceException("Failed to parse WMTS capabilities, layerId: "
                    + layer.getId(), e);
        }
    }

}

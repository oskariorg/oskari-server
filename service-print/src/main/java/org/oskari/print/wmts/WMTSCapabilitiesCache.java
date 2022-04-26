package org.oskari.print.wmts;

import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.cache.Cache;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.RawCapabilitiesResponse;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.wmts.WMTSCapabilities;
import org.oskari.capabilities.ogc.wmts.WMTSCapabilitiesParserHelper;
import org.oskari.print.request.PrintLayer;

/**
 * Caches parsed WMTSCapabilities
 */
public class WMTSCapabilitiesCache {

    private final Cache<WMTSCapabilities> wmtsCapabilitiesCache;

    public WMTSCapabilitiesCache() {
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
            ServiceConnectInfo info = new ServiceConnectInfo(layer.getUrl(), OskariLayer.TYPE_WMTS, layer.getVersion());
            info.setCredentials(layer.getUsername(), layer.getPassword());

            RawCapabilitiesResponse resp = CapabilitiesService.getCapabilities(info);
            return WMTSCapabilitiesParserHelper.parseCapabilities(new String(resp.getResponse()));
        } catch (Exception e) {
            throw new ServiceException("Failed to parse WMTS capabilities, layerId: "
                    + layer.getId(), e);
        }
    }

}

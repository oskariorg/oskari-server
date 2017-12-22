package org.oskari.print.wmts;

import fi.nls.oskari.service.ServiceException;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import java.util.Map;
import java.util.Optional;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.util.LRUCache;

/**
 * Caches TileMatrixSets from WMTSCapabilities
 */
public class TileMatrixSetCache {

    private static final Logger LOG = LogFactory.getLogger(TileMatrixSetCache.class);

    private final CapabilitiesCacheService capabilitiesService;
    private final Map<String, TileMatrixSet> cache;

    public TileMatrixSetCache() {
        this(new CapabilitiesCacheServiceMybatisImpl());
    }

    public TileMatrixSetCache(CapabilitiesCacheService capabilitiesService) {
        this.capabilitiesService = capabilitiesService;
        this.cache = LRUCache.createLRUCache(64);
    }

    public Optional<TileMatrixSet> get(PrintLayer layer) throws ServiceException {
        if (!OskariLayer.TYPE_WMTS.equals(layer.getType())) {
            return Optional.empty();
        }

        final String key = getKey(layer.getId(), layer.getTileMatrixSet());
        TileMatrixSet set = cache.get(key);
        if (set == null) {
            parseCabalitiesToCache(layer);
            set = cache.get(key);
        }
        return Optional.ofNullable(set);
    }

    private String getKey(String id, String tileMatrixSet) {
        return id + "_" + tileMatrixSet;
    }

    private void parseCabalitiesToCache(PrintLayer layer) throws ServiceException {
        OskariLayerCapabilities xml = capabilitiesService.getCapabilities(layer.getUrl(),
                layer.getType(), layer.getUsername(), layer.getPassword(), layer.getVersion());

        WMTSCapabilities caps;
        try {
            caps = new WMTSCapabilitiesParser().parseCapabilities(xml.getData());
        } catch (Exception e) {
            throw new ServiceException("Failed to parse WMTS capabilities, layerId: "
                    + layer.getId(), e);
        }

        for (TileMatrixSet tileMatrixSet : caps.getTileMatrixSets()) {
            String key = getKey(layer.getId(), tileMatrixSet.getId());
            LOG.debug("Adding", key, "to cache");
            cache.put(key, tileMatrixSet);
        }
    }

}
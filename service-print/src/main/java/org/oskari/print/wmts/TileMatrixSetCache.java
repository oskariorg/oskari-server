package org.oskari.print.wmts;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oskari.print.request.PrintLayer;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.wmts.domain.TileMatrixSet;

/**
 * Parses TileMatrixSet information from WMTSCapabilities
 * and caches the results in a map   
 */
public class TileMatrixSetCache {

    private static final Logger LOG = LogFactory.getLogger(TileMatrixSetCache.class);

    private static final Map<String, TileMatrixSet> CACHE = new ConcurrentHashMap<>();

    public static TileMatrixSet get(PrintLayer layer) {
        if (!OskariLayer.TYPE_WMTS.equals(layer.getType())) {
            return null;
        }

        String key = getKey(layer.getId(), layer.getTileMatrixSet());
        TileMatrixSet set = CACHE.get(key);
        if (set == null) {
            parse(layer);
            set = CACHE.get(key);
        }
        return set;
    }

    private static void parse(PrintLayer layer) {
        String uri = getWMTSGetCapabilitiesUri(layer.getUrl());
        if (uri != null) {
            List<TileMatrixSet> tileMatrixSets = WMTSTileMatrixSetParser.parse(uri);
            if (tileMatrixSets != null) {
                for (TileMatrixSet tileMatrixSet : tileMatrixSets) {
                    String key = getKey(layer.getId(), tileMatrixSet.getId());
                    LOG.debug("Adding", key, "to cache");
                    CACHE.put(key, tileMatrixSet);
                }
            }
        }
    }

    protected static String getWMTSGetCapabilitiesUri(String url) {
        int i = url.indexOf("/1.0.0/");
        if (i < 0) {
            return null;
        }
        i += "/1.0.0/".length();
        return url.substring(0, i) + "WMTSCapabilities.xml";
    }

    private static String getKey(String layerId, String tileMatrixSetId) {
        return layerId + "_" + tileMatrixSetId;
    }

}

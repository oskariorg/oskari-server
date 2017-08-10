package org.oskari.capabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;

public class CapabilitiesCacheServiceInMemory extends CapabilitiesCacheService {

    private final List<OskariLayerCapabilities> list = new ArrayList<>();
    
    @Override
    public OskariLayerCapabilities find(String url, String layertype,
            String version) {
        for (OskariLayerCapabilities i : list) {
            
        }
        return index.get(getIndexKey(url, layertype, version));
    }

    @Override
    public OskariLayerCapabilities save(OskariLayerCapabilities capabilities) {
        final long id = capabilities.getId();
        if (id > 0L) {
            if (id < list.size()) {
                update(id, capabilities);
            }
        } else {
            return insert(capabilities);
        }
        return capabilities;
    }

    private synchronized OskariLayerCapabilities insert(OskariLayerCapabilities capabilities) {
        return null;
    }

    private OskariLayerCapabilities update(final long id, OskariLayerCapabilities capabilities) {
        list.set(id, capabilities);
    }
    
    private String getIndexKey(String url, String layertype, String version) {
        return url.toLowerCase()
                + "_" + layertype.toLowerCase()
                + "_" + version.toLowerCase();
    }

}

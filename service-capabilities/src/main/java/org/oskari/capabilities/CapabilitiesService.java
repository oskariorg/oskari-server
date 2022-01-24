package org.oskari.capabilities;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import org.oskari.capabilities.ogc.OGCCapabilitiesParser;

import java.util.Map;


public class CapabilitiesService {

    public static Map<String, LayerCapabilities> getLayersFromService(OskariLayer layer)
            throws ServiceException {
        String layerType = layer.getType();
        OGCCapabilitiesParser parser = getParser(layerType);
        if (parser == null) {
            throw new ServiceException("Unrecognized type: " + layerType);
        }
        return parser.getLayersFromService(layer);
    }

    private static OGCCapabilitiesParser getParser(String layerType) {
        return (OGCCapabilitiesParser) OskariComponentManager
                .getComponentsOfType(OGCCapabilitiesParser.class)
                .get(layerType);
    }
}

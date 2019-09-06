package org.oskari.permissions.model;

import fi.nls.oskari.domain.map.OskariLayer;

/**
 * Convenience mapping of oskari-permission/Resource for OskariLayer
 */
public class OskariLayerResource extends Resource {

    public OskariLayerResource(OskariLayer layer) {
        this(Integer.toString(layer.getId()));
    }

    public OskariLayerResource(String layerId) {
        setMapping(layerId);
    }

}

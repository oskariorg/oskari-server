package org.oskari.permissions.model;

import fi.nls.oskari.domain.map.OskariLayer;

/**
 * Convenience mapping of oskari-permission/Resource for OskariLayer
 * @deprecated Use plain Resource instead as layer id is used as mapping.
 */
@Deprecated
public class OskariLayerResource extends Resource {

    public OskariLayerResource(OskariLayer layer) {
        this(layer.getId());
    }

    public OskariLayerResource(int layerId) {
        this(Integer.toString(layerId));
    }

    public OskariLayerResource(String layerId) {
        setMapping(layerId);
        setType(ResourceType.maplayer);
    }

}

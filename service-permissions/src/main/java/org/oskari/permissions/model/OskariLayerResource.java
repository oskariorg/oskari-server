package org.oskari.permissions.model;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceRuntimeException;

/**
 * Convenience mapping of oskari-permission/Resource for OskariLayer
 * @deprecated Use Resource instead as layer id is used for mapping this has become unnecessary.
 */
@Deprecated
public class OskariLayerResource extends Resource {

    public OskariLayerResource(OskariLayer layer) {
        this(layer.getId());
    }

    public OskariLayerResource(int layerId) {
        this(Integer.toString(layerId));
    }

    public OskariLayerResource(String type, String url, String name) {
        // so old code doesn't need to be modified and signals problem on migrations etc
        throw new ServiceRuntimeException("This is no longer supported. Use layer id as mapping instead");
    }

    public OskariLayerResource(String layerId) {
        setMapping(layerId);
        setType(ResourceType.maplayer);
    }

}

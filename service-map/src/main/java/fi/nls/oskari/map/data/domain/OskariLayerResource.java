package fi.nls.oskari.map.data.domain;

import fi.mml.portti.domain.permissions.Permissions;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.permission.domain.Resource;

/**
 * Convenience mapping of oskari-permission/Resource for OskariLayer
 */
public class OskariLayerResource extends Resource {

    public OskariLayerResource(OskariLayer layer) {
        setType(Permissions.RESOURCE_TYPE_MAP_LAYER);
        setMapping(layer.getType() + "+" + layer.getUrl(), layer.getName());
    }
}

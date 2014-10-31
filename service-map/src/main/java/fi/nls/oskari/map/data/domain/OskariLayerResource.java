package fi.nls.oskari.map.data.domain;

import fi.mml.portti.domain.permissions.Permissions;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.permission.domain.Resource;

/**
 * Convenience mapping of oskari-permission/Resource for OskariLayer
 */
public class OskariLayerResource extends Resource {

    private String namespace = "";
    private String name = "";

    public OskariLayerResource(OskariLayer layer) {
        setType(Permissions.RESOURCE_TYPE_MAP_LAYER);
        namespace = layer.getType() + "+" + layer.getUrl();
        name = layer.getName();
        setMapping(namespace, name);
    }

    public String getNamespace() {
        return namespace;
    }
    public String getName() {
        return name;
    }
}

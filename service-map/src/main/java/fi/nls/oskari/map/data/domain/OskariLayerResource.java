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
        this(layer.getType(), layer.getUrl(), layer.getName());
    }

    public OskariLayerResource(String type, String url, String name) {
        setType(Permissions.RESOURCE_TYPE_MAP_LAYER);
        namespace = type + "+" + url;
        this.name = name;
        setMapping(namespace, name);
    }

    public String getNamespace() {
        return namespace;
    }
    public String getName() {
        return name;
    }
}

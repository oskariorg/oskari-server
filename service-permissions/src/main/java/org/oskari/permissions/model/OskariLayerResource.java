package org.oskari.permissions.model;

import fi.nls.oskari.domain.map.OskariLayer;

/**
 * Convenience mapping of oskari-permission/Resource for OskariLayer
 */
public class OskariLayerResource extends Resource {

    private final String namespace;
    private final String name;

    public OskariLayerResource(OskariLayer layer) {
        this(layer.getType(), layer.getUrl(), layer.getName());
    }

    public OskariLayerResource(String type, String url, String name) {
        setType(ResourceType.maplayer);
        this.namespace = type + "+" + url;
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

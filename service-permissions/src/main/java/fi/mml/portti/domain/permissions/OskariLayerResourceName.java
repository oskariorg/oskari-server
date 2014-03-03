package fi.mml.portti.domain.permissions;

import fi.nls.oskari.domain.map.OskariLayer;

public class OskariLayerResourceName extends UniqueResourceName {

    public OskariLayerResourceName(final OskariLayer layer) {
        setType(Permissions.RESOURCE_TYPE_MAP_LAYER);
        setName(layer.getName());
        setNamespace(layer.getUrl());
    }
}

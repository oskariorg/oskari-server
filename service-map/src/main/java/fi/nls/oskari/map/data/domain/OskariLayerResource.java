package fi.nls.oskari.map.data.domain;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.permission.domain.Resource;

/**
 * Convenience mapping of oskari-permission/Resource for OskariLayer
 */
public class OskariLayerResource extends Resource {

    public OskariLayerResource(OskariLayer layer) {
        setMapping(layer.getUrl(), layer.getName());
    }
}

package org.oskari.service.maplayer;

import org.oskari.domain.map.LayerExtendedOutput;
import org.oskari.domain.map.LayerOutput;
import org.oskari.user.User;

import java.util.List;

import fi.nls.oskari.service.OskariComponent;

public abstract class LayerProvider extends OskariComponent {

    public abstract boolean maybeProvides(String layerId);
    public abstract List<LayerOutput> listLayers(User user, String lang);
    public abstract LayerExtendedOutput describeLayer(DescribeLayerQuery query) throws SecurityException;

}

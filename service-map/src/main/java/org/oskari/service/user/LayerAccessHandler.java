package org.oskari.service.user;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponent;

public abstract class LayerAccessHandler extends OskariComponent {

    public abstract void handle(OskariLayer layer, User user);

}

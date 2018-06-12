package org.oskari.control.userlayer;

import org.oskari.map.userlayer.service.UserLayerDbService;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.userlayer.UserLayer;

public class UserLayerHandlerHelper {

    public static UserLayer getUserLayer(UserLayerDbService service, ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        long id = params.getRequiredParamLong(ActionConstants.KEY_ID);
        UserLayer userLayer = service.getUserLayerById(id);
        if (userLayer == null) {
            throw new ActionParamsException("UserLayer doesn't exist: " + id);
        }
        if (!userLayer.isOwnedBy(params.getUser().getUuid())) {
            throw new ActionDeniedException("UserLayer belongs to another user");
        }
        return userLayer;
    }

}

package org.oskari.control.userlayer;

import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.map.userlayer.service.UserLayerDbService;


/**
 * Expects to get layer id as http parameter "id".
 */
@OskariActionRoute("GetUserLayerStyle")
public class GetUserLayerStyleHandler extends ActionHandler {

    private UserLayerDbService userLayerDbService;

    @Override
    public void init() {
        userLayerDbService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
    }

    public void handleAction(ActionParameters params) throws ActionException {
        UserLayer layer = UserLayerHandlerHelper.getUserLayer(userLayerDbService, params);
        UserLayerStyle style = userLayerDbService.getUserLayerStyleById(layer.getId());
        if (style == null) {
            throw new ActionParamsException("Unable to get style for id" + layer.getId());
        }
        JSONObject response = style.parseUserLayerStyle2JSON();
        ResponseHelper.writeResponse(params, response);
    }

}

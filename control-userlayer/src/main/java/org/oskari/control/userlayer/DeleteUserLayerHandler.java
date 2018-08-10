package org.oskari.control.userlayer;

import fi.nls.oskari.control.RestActionHandler;
import org.json.JSONObject;
import org.oskari.map.userlayer.service.UserLayerDbService;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Deletes user layer and its style if it belongs to current user.
 * Expects to get layer id as http parameter "id".
 */
@OskariActionRoute("DeleteUserLayer")
public class DeleteUserLayerHandler extends RestActionHandler {

    private UserLayerDbService userLayerDbService;

    public void setUserLayerDbService(UserLayerDbService service) {
        userLayerDbService = service;
    }

    @Override
    public void init() {
        if (userLayerDbService == null) {
            userLayerDbService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        try {
            UserLayer userLayer = UserLayerHandlerHelper.getUserLayer(userLayerDbService, params);
            userLayerDbService.deleteUserLayer(userLayer);
            JSONObject response = JSONHelper.createJSONObject("result", "success");
            ResponseHelper.writeResponse(params, response);
        } catch (ServiceException ex) {
            throw new ActionException("Error deleting userLayer", ex);
        }
    }

}

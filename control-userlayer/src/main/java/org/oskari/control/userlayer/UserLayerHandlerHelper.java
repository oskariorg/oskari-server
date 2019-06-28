package org.oskari.control.userlayer;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.oskari.map.userlayer.service.UserLayerDbService;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import org.oskari.permissions.model.PermissionType;

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

    public static JSONObject getPermissions() {
        final JSONObject permissions = new JSONObject();
        JSONHelper.putValue(permissions, PermissionType.PUBLISH.getJsonKey(), OskariLayerWorker.PUBLICATION_PERMISSION_OK);
        JSONHelper.putValue(permissions, PermissionType.DOWNLOAD.getJsonKey(), OskariLayerWorker.DOWNLOAD_PERMISSION_OK);
        return permissions;
    }
}

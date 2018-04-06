package org.oskari.control.userlayer;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;

import java.util.List;

/**
 * Returns users user data layers as JSON.
 */
@OskariActionRoute("GetUserLayers")
public class GetUserLayersHandler extends ActionHandler {

    private UserLayerDbService userLayerService;

    private static final String JSKEY_USERLAYERS = "userlayers";

    public void init() {
        userLayerService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final JSONObject response = new JSONObject();
        final JSONArray layers = new JSONArray();
        JSONHelper.putValue(response, JSKEY_USERLAYERS, layers);

        final User user = params.getUser();
        if (!user.isGuest()) {
            final List<UserLayer> list = userLayerService.getUserLayerByUuid(user.getUuid());
            final OskariLayer baseLayer = UserLayerDataService.getBaseLayer();
            for (UserLayer ul : list) {
                // Parse userlayer data to userlayer
                final JSONObject userLayer = UserLayerDataService.parseUserLayer2JSON(ul, baseLayer);
                JSONObject permissions = OskariLayerWorker.getAllowedPermissions();
                JSONHelper.putValue(userLayer, "permissions", permissions);
                // transform WKT for layers now that we know SRS
                OskariLayerWorker.transformWKTGeom(userLayer, params.getHttpParam(ActionConstants.PARAM_SRS));
                layers.put(userLayer);
            }
        }

        ResponseHelper.writeResponse(params, response);
    }
}

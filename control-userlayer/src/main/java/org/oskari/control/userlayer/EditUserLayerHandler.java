package org.oskari.control.userlayer;

import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import org.oskari.log.AuditLog;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.map.userlayer.service.UserLayerException;

/**
 * Expects to get layer id as http parameter "id".
 */
@OskariActionRoute("EditUserLayer")
public class EditUserLayerHandler extends RestActionHandler {

    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_STYLE = "style";

    private UserLayerDbService userLayerDbService;

    @Override
    public void init() {
        userLayerDbService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        String mapSrs = params.getHttpParam(ActionConstants.PARAM_SRS);
        final UserLayer userLayer = UserLayerHandlerHelper.getUserLayer(userLayerDbService, params);
        JSONObject payload = params.getPayLoadJSON();
        userLayer.setLocale(JSONHelper.getJSONObject(payload, PARAM_LOCALE));
        WFSLayerOptions wfsOptions = userLayer.getWFSLayerOptions();
        wfsOptions.setDefaultFeatureStyle(JSONHelper.getJSONObject(payload, PARAM_STYLE));
        try {
            userLayerDbService.updateUserLayer(userLayer);
        } catch (UserLayerException e) {
            throw new ActionException("Failed to update", e);
        }

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", userLayer.getId())
                .updated(AuditLog.ResourceType.USERLAYER);

        JSONObject ulayer = UserLayerDataService.parseUserLayer2JSON(userLayer, mapSrs);
        JSONObject permissions = UserLayerHandlerHelper.getPermissions();
        JSONHelper.putValue(ulayer, "permissions", permissions);

        ResponseHelper.writeResponse(params, ulayer);
    }
}

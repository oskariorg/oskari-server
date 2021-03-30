package org.oskari.control.userlayer;

import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.PropertyUtil;
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

    private static final String PARAM_DESC = "desc";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_SOURCE = "source";
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
        userLayer.setName(PropertyUtil.getDefaultLanguage(), params.getRequiredParam(PARAM_NAME)); // FIXME: userLayer.setLocale(locale);
        userLayer.setLayer_desc(params.getHttpParam(PARAM_DESC, userLayer.getLayer_desc()));
        userLayer.setLayer_source(params.getHttpParam(PARAM_SOURCE, userLayer.getLayer_source()));
        WFSLayerOptions wfsOptions = userLayer.getWFSLayerOptions();
        wfsOptions.setDefaultFeatureStyle(JSONHelper.createJSONObject(params.getHttpParam(PARAM_STYLE)));
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

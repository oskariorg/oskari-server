package org.oskari.control.userlayer;

import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;

/**
 * Expects to get layer id as http parameter "id".
 */
@OskariActionRoute("EditUserLayer")
public class EditUserLayerHandler extends ActionHandler {
    private static final String PARAM_ID = "id";
    private static final String PARAM_DESC = "desc";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_SOURCE = "source";
    private static final String PARAM_STYLE = "style";
    
    private UserLayerDbService userLayerDbService;
    private final UserLayerDataService userlayerService = new UserLayerDataService();


    @Override
    public void init() {
        userLayerDbService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        final long id = params.getRequiredParamLong(PARAM_ID);
        
        final UserLayer userLayer = userLayerDbService.getUserLayerById(id);
        final UserLayerStyle style = new UserLayerStyle();

        if(userLayer == null) {
            throw new ActionParamsException("Userlayer id doesn't exist: " + id);
        }
        if(!userLayer.isOwnedBy(params.getUser().getUuid())) {
            throw new ActionDeniedException("Userlayer belongs to another user");
        }
               
        userLayer.setLayer_name(params.getRequiredParam(PARAM_NAME));
        userLayer.setLayer_desc(params.getHttpParam(PARAM_DESC, userLayer.getLayer_desc()));
        userLayer.setLayer_source(params.getHttpParam(PARAM_SOURCE, userLayer.getLayer_source()));
        
        try {
            final JSONObject stylejs = JSONHelper
                .createJSONObject(params.getHttpParam(PARAM_STYLE));
            style.setId(id);
            style.populateFromJSON(stylejs);
        } catch (JSONException e) {
            throw new ActionParamsException("Unable to populate style from JSON", e);
        }

        userLayerDbService.updateUserLayerCols(userLayer);
        userLayerDbService.updateUserLayerStyleCols(style);

        JSONObject ulayer = userlayerService.parseUserLayer2JSON(userLayer);
        JSONObject permissions = OskariLayerWorker.getAllowedPermissions();
        JSONHelper.putValue(ulayer, "permissions", permissions);

        ResponseHelper.writeResponse(params, ulayer);
    }
     
}

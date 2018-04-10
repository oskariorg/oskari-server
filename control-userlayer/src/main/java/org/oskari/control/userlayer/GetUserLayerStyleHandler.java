package org.oskari.control.userlayer;

import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
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
    private static final String PARAM_ID = "id";
    private UserLayerDbService userLayerDbService;
    
    @Override
    public void init() {
        userLayerDbService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
    }
    
    public void handleAction(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        final long id = params.getRequiredParamLong(PARAM_ID);
        //style id == userlayer id
        final UserLayer userLayer = userLayerDbService.getUserLayerById(id);
        if(userLayer == null) {
            throw new ActionParamsException("Userlayer's style id didn't match any userlayer: " + id);
        }
        if(!userLayer.isOwnedBy(params.getUser().getUuid())) {
            throw new ActionDeniedException("Userlayer's style belongs to another user");
        }

        final UserLayerStyle style = userLayerDbService.getUserLayerStyleById(id);
        if (style != null){
            JSONObject response = style.parseUserLayerStyle2JSON();
            ResponseHelper.writeResponse(params, response);
        }else{
            throw new ActionParamsException("Unable to get style for id" + id);
        }        
    }    
}

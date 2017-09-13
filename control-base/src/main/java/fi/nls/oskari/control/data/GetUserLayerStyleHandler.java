package fi.nls.oskari.control.data;

import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.map.userlayer.service.UserLayerDbService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.ResponseHelper;


/**
 * Expects to get layer id as http parameter "id".
 */
@OskariActionRoute("GetUserLayerStyle")
public class GetUserLayerStyleHandler extends ActionHandler {
    private final static String PARAM_ID = "id";
    private UserLayerDbService userLayerDbService = null;
    
    @Override
    public void init() {
        super.init();
        if(userLayerDbService == null) {
            userLayerDbService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
        }
    }
    
    public void handleAction(ActionParameters params) throws ActionException {
        if(params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }
        final long id = ConversionHelper.getLong(params.getHttpParam(PARAM_ID), -1);

        if(id == -1) {
            throw new ActionParamsException("Parameter missing or non-numeric: " + PARAM_ID + "=" + params.getHttpParam(PARAM_ID));
        }
        final UserLayerStyle style = userLayerDbService.getUserLayerStyleById(id);
        if (style != null){
            JSONObject response = style.parseUserLayerStyle2JSON();
            ResponseHelper.writeResponse(params, response);
        }else{
            throw new ActionException("Unable to get style for id" + id);
        }        
    }    
}

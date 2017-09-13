package fi.nls.oskari.control.data;

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
import fi.nls.oskari.map.userlayer.service.UserLayerDataService;
import fi.nls.oskari.map.userlayer.service.UserLayerDbService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Expects to get layer id as http parameter "id".
 */
@OskariActionRoute("EditUserLayer")
public class EditUserLayerHandler extends ActionHandler {
    private final static String PARAM_ID = "id";
    private static final String PARAM_DESC = "desc";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_SOURCE = "source";
    private static final String PARAM_STYLE = "style";
    
    private UserLayerDbService userLayerDbService = null;
    private final UserLayerDataService userlayerService = new UserLayerDataService();
    
    public void setUserLayerDbService(final UserLayerDbService service) {
        userLayerDbService = service;
    }

    @Override
    public void init() {
        super.init();
        if(userLayerDbService == null) {
            setUserLayerDbService(OskariComponentManager.getComponentOfType(UserLayerDbService.class));
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        final long id = ConversionHelper.getLong(params.getHttpParam(PARAM_ID), -1);

        if(id == -1) {
            throw new ActionParamsException("Parameter missing or non-numeric: " + PARAM_ID + "=" + params.getHttpParam(PARAM_ID));
        }
        
        final UserLayer userLayer = userLayerDbService.getUserLayerById(id);
        final UserLayerStyle style = new UserLayerStyle();

        if(userLayer == null) {
            throw new ActionParamsException("User layer id didn't match any user layer: " + id);
        }
        if(!userLayer.isOwnedBy(params.getUser().getUuid())) {
            throw new ActionDeniedException("User layer belongs to another user");
        }
               
        userLayer.setLayer_name(params.getHttpParam(PARAM_NAME));
        userLayer.setLayer_desc(params.getHttpParam(PARAM_DESC));
        userLayer.setLayer_source(params.getHttpParam(PARAM_SOURCE));
        
        try {
            final JSONObject stylejs = JSONHelper
                .createJSONObject(params.getHttpParam(PARAM_STYLE));
            style.setId(id);
            style.populateFromJSON(stylejs);
        } catch (JSONException e) {
            throw new ActionException("Unable to populate style from JSON", e);
        }

        userLayerDbService.updateUserLayerCols(userLayer);
        userLayerDbService.updateUserLayerStyleCols(style);

        JSONObject ulayer = userlayerService.parseUserLayer2JSON(userLayer);
        JSONObject permissions = OskariLayerWorker.getAllowedPermissions();
        JSONHelper.putValue(ulayer, "permissions", permissions);

        ResponseHelper.writeResponse(params, ulayer);
    }
     
}

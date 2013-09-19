package fi.nls.oskari.control.layer;

import javax.servlet.http.HttpServletResponse;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.log.LogFactory;
import org.json.JSONObject;

import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.WFSLayerConfiguration;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

@OskariActionRoute("GetWFSLayerConfiguration")
public class GetWFSLayerConfigurationHandler extends ActionHandler {

	private static final Logger log = LogFactory.getLogger(GetWFSLayerConfigurationHandler.class);
	
    private final WFSLayerConfigurationService layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();

    private final static String ID = "id";
    
    private final static String ERROR = "error";
    private final static String ERROR_NO_ID = "id parameter wasn't given";
    private final static String ERROR_NOT_FOUND = "id wasn't found";
    private final static String ERROR_NO_PERMISSION = "no permissions to view the layer";

    public void handleAction(ActionParameters params) throws ActionException {

		final JSONObject root = new JSONObject();
		
        final int id = ConversionHelper.getInt(params.getHttpParam(ID), 0);
        
        final HttpServletResponse response = params.getResponse();            
        response.setContentType("application/json");
        
        PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
        if(!permissionsService.hasViewPermissionForLayerByLayerId(params.getUser(), id)) {
    		JSONHelper.putValue(root, ERROR, ERROR_NO_PERMISSION);
    		ResponseHelper.writeResponse(params,root);
            // FIXME: throw ActionDeniedException instead and modify client response parsing
    		return;
        }
        
        if(id == 0) {
    		JSONHelper.putValue(root, ERROR, ERROR_NO_ID);
    		ResponseHelper.writeResponse(params,root);
            // FIXME: throw ActionParamsException instead and modify client response parsing
    		return;
        }
        
        String json = WFSLayerConfiguration.getCache(id + "");
        if(json == null) {
        	WFSLayerConfiguration lc = layerConfigurationService.findConfiguration(id);
            if(lc == null) {
        		JSONHelper.putValue(root, ERROR, ERROR_NOT_FOUND);
        		ResponseHelper.writeResponse(params,root);
                // FIXME: throw ActionParamsException instead and modify client response parsing
        		return;
            }
        	json = lc.getAsJSON();
        	lc.save();
        }
		ResponseHelper.writeResponse(params,json);
	}
}

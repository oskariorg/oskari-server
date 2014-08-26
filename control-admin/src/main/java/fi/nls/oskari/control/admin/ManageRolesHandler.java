package fi.nls.oskari.control.admin;

import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("ManageRoles")
public class ManageRolesHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(ManageRolesHandler.class);
    final private static String INSERT = "insert";
    final private static String DELETE = "delete";
    final private static String ADD = "add"; 
    
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
    	
        
        log.debug("Managing roles:");
        
        String id = params.getHttpParam("id");
    	String roleId = params.getHttpParam("roleid");
    	String action = params.getHttpParam("action");
    	String test = params.getHttpParam("test");
        try{
        	if(test != null){
	        	if(action.equals(INSERT)){
	        		log.debug("inserting a role");
	            	UserService.getInstance().insertRole(roleId, id);
	        	}else if(action.equals(DELETE)){
	        		log.debug("deleting a role");
	        		UserService.getInstance().insertRole(roleId, id);
	        	}else if(action.equals(ADD)){
	        		log.debug("adding a role");
	        		UserService.getInstance().insertRole(roleId, id);
	        	}else{
	        		// TODO: give error message
	        	}
        	}
        	
          final JSONObject response = new JSONObject();
          JSONHelper.putValue(response, "id", 2);
          JSONHelper.putValue(response, "name", "User");
        	
          ResponseHelper.writeResponse(params, response);
       	
        }catch(Exception e){
        	e.printStackTrace();
        	throw new ActionException("douh!");
        }

    }
    

}

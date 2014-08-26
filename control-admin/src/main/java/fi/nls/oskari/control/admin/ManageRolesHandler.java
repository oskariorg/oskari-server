package fi.nls.oskari.control.admin;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
    final private static String GETROLES = "getRoles";
    final private static String ADD = "add"; 
    
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
    	
        
        log.debug("Managing roles:");
        
        String id = params.getHttpParam("id");
    	String roleId = params.getHttpParam("roleid");
    	String action = params.getHttpParam("action");
    	if(action == null)
    		throw new ActionException("action parameter was null");
    	
    	String test = params.getHttpParam("test");
    	final JSONObject response = new JSONObject();
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
        	}else{
        		if(action.equals(INSERT)){
	        		log.debug("inserting a role");
	                JSONHelper.putValue(response, "id", 2);
	                JSONHelper.putValue(response, "name", "User");
	        	}else if(action.equals(DELETE)){
	        		log.debug("deleting a role");
	                JSONHelper.putValue(response, "id", 1);
	                JSONHelper.putValue(response, "name", "Guest");
	        	}else if(action.equals(GETROLES)){
	        		log.debug("getting roles");
	                
	                List<String> valueList = new ArrayList<String>();
	                valueList.add("Guest");
	                valueList.add("User");
	                valueList.add("Pekka");
	                final JSONArray roleValues = new JSONArray(valueList);
	                JSONHelper.put(response, "rolelist", roleValues);
	        		
	        	}else{
	        		// TODO: give error message
	        	}
           }
        	
        	
          ResponseHelper.writeResponse(params, response);
       	
        }catch(Exception e){
        	e.printStackTrace();
        	throw new ActionException("error in ManageRoles");
        }

    }
    

    
}

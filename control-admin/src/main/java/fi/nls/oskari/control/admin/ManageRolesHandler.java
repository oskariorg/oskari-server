package fi.nls.oskari.control.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.user.DatabaseUserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("ManageRoles")
public class ManageRolesHandler extends RestActionHandler {

    private static final Logger log = LogFactory.getLogger(ManageRolesHandler.class);
    
    final private static String ROLE_NAME = "name";
    final private static String ROLE_ID = "id";
    
    
    private DatabaseUserService databaseUserService = null;

    @Override
    public void init() {
        try {
        	databaseUserService = DatabaseUserService.getInstance();
        } catch (ServiceException se) {
            log.error(se, "Unable to initialize User service!");
        }
    }    
    
    
    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        log.info("handleGet");
        
        if(params.getHttpParam("test") == null)
        	preProcess(params);

        Role[] roles = null;
        try {
        	roles = databaseUserService.getRoles(Collections.emptyMap());

        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
        JSONObject response = null;
        try {
            response = roles2JsonArray(roles);
        } catch (JSONException je) {
            throw new ActionException(je.getMessage(), je);
        }
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        log.debug("handlePut");
        if(params.getHttpParam("test") == null)
        	preProcess(params);

        String roleName = params.getHttpParam(ROLE_NAME);
        log.debug("Name: " + roleName);

        Role role = null;
        if (roleName != null) {
        	try {
               role =  databaseUserService.insertRole(roleName);
            } catch (ServiceException se) {
            	throw new ActionException(se.getMessage(), se);
            }
        }else{
        	throw new ActionException("Parameter " + ROLE_NAME + " not found.");
        }
        JSONObject response = null;
        try {
            response = role2Json(role);
        } catch (JSONException je) {
            throw new ActionException(je.getMessage(), je);
        }
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        log.debug("handleDelete");
        if(params.getHttpParam("test") == null)
        	preProcess(params);

        log.debug("roleId: " + params.getHttpParam(ROLE_ID));
        
        
        int id = getId(params);
        if (id > -1) {
            try {
            	databaseUserService.deleteRole(id);
            } catch (ServiceException se) {
                throw new ActionException(se.getMessage(), se);
            }
        } else {
            throw new ActionException("Parameter 'id' not found.");
        }
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
    	log.debug("Manage Roles preproses");
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }
    
    
    private int getId(ActionParameters params) throws NumberFormatException {
        // see if params contains an ID
        int id = -1;
        String idString = params.getHttpParam(ROLE_ID, "-1");
        if (idString != null && idString.length() > 0) {
            id = Integer.parseInt(idString);
        }
        return id;
    } 

    private JSONObject roles2JsonArray(Role[] roles) throws JSONException {
    	
        List<JSONObject> valueList = new ArrayList<JSONObject>();
        JSONObject response = new JSONObject();
        
        JSONObject tmp = null;
        for(Role role : roles){
            log.debug("id:name: " + role.getId() + " : " + role.getName());
        	tmp = new JSONObject();
        	JSONHelper.putValue(tmp, "id", role.getId());
        	JSONHelper.putValue(tmp, "name", role.getName());
        	valueList.add(tmp);
        }
        final JSONArray roleValues = new JSONArray(valueList);
        
        JSONHelper.put(response, "rolelist", roleValues);
        
        return response;
    }
    
    private JSONObject role2Json(Role role) throws JSONException {
        JSONObject ro = new JSONObject();
        ro.put("id", role.getId());
        ro.put("name", role.getName());
        return ro;
    }    
  
}

package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

@OskariActionRoute("ManageRoles")
public class ManageRolesHandler extends RestActionHandler {

    private static final Logger log = LogFactory.getLogger(ManageRolesHandler.class);
    
    final private static String ROLE_NAME = "name";
    final private static String ROLE_ID = "id";

    private UserService userService = null;

    @Override
    public void init() {
        try {
        	userService = UserService.getInstance();
        } catch (ServiceException se) {
            log.error(se, "Unable to initialize User service!");
        }
    }    
    
    
    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        Role[] roles = null;
        try {
        	roles = userService.getRoles(Collections.emptyMap());

        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
        try {
            ResponseHelper.writeResponse(params, roles2JsonArray(roles));
        } catch (JSONException je) {
            throw new ActionException(je.getMessage(), je);
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {

        final String roleName = params.getRequiredParam(ROLE_NAME);
        log.debug("Inserting role with name:", roleName);

        try {
            final Role role =  userService.insertRole(roleName);
            ResponseHelper.writeResponse(params, role2Json(role));
        } catch (Exception se) {
            throw new ActionException(se.getMessage(), se);
        }
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        log.debug("handleDelete");
        final int id = params.getRequiredParamInt(ROLE_ID);
        if (id < 0) {
            throw new ActionException("Parameter 'id' value can't be negative.");
        }
        try {
            userService.deleteRole(id);
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

    private JSONObject roles2JsonArray(Role[] roles) throws JSONException {

        final JSONArray roleValues = new JSONArray();
        for(Role role : roles){
            roleValues.put(role2Json(role));
        }

        final JSONObject response = new JSONObject();
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

package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
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
import org.oskari.log.AuditLog;
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
        final long id = params.getHttpParam(ROLE_ID, 0);
        log.debug("Inserting role with name:", roleName);

        try {
            Role role;
            AuditLog audit = AuditLog.user(params.getClientIp(), params.getUser());
            if (id > 0) {
                role = userService.updateRole(id, roleName);
                audit.withParam("id", role.getId())
                    .withParam("name", role.getName())
                    .withMsg("Role")
                    .updated(AuditLog.ResourceType.USER);
            } else {
                role = userService.insertRole(roleName);
                audit.withParam("id", role.getId())
                    .withParam("name", role.getName())
                    .withMsg("Role")
                    .added(AuditLog.ResourceType.USER);
            }
            ResponseHelper.writeResponse(params, role.toJSON());
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
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", id)
                    .withMsg("Role")
                    .deleted(AuditLog.ResourceType.USER);
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireAdminUser();
    }

    private JSONObject roles2JsonArray(Role[] roles) throws JSONException {

        final JSONArray roleValues = new JSONArray();
        if (roles != null) {
            for (Role role : roles) {
                roleValues.put(role.toJSON());
            }
        }

        final JSONObject response = new JSONObject();
        JSONHelper.put(response, "rolelist", roleValues);
        JSONHelper.putValue(response, "systemRoles", Role.getSystemRolesAsMap());

        return response;
    }
}

package fi.nls.oskari.control.admin;

import static fi.nls.oskari.control.ActionConstants.KEY_ID;
import static fi.nls.oskari.control.ActionConstants.KEY_NAME;

import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.PermissionType;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetAllRolesAndPermissionTypes")
public class GetAllRolesAndPermissionTypesHandler extends RestActionHandler {
    
    private Logger log = LogFactory.getLogger(GetAllRolesAndPermissionTypesHandler.class);
    private UserService userService = null;
    private PermissionService permissionsService = null;

    private final static String JSKEY_ID = "id";
    private final static String JSKEY_NAME = "name";
    private final static String JSKEY_ROLES = "roles";
    private final static String JSKEY_PERMISSION_TYPES = "permissionTypes";

    @Override
    public void init() {
        try {
            userService = UserService.getInstance();
        } catch (Exception ex) {
            log.error(ex, "Unable to initialize User service!");
        }
        try {
            permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
        } catch (Exception ex) {
            log.error(ex, "Unable to initialize permission service!");
        }
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireAdminUser();

        if (userService == null) {
            throw new ActionParamsException("User service not initialized");
        }
        if (permissionsService == null) {
            throw new ActionParamsException("PermissionsService service not initialized");
        }
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        try {
            
            final JSONObject root = new JSONObject();
            getRoles(root);
            getPermissionTypes(root,params.getLocale().getLanguage());
            ResponseHelper.writeResponse(params, root);
        } catch (Exception e) {
            throw new ActionException("Something went wrong getting roles and permission types from the platform", e);
        }
    }

    private void getRoles(final JSONObject root) throws ServiceException, JSONException  {
        final Role[] roles = userService.getRoles();

        final JSONArray rolesJSON = new JSONArray();

        for (Role role : roles) {
            JSONObject external = new JSONObject();
            external.put(JSKEY_ID, role.getId());
            external.put(JSKEY_NAME, role.getName());
            rolesJSON.put(external);
        }

        
        root.put(JSKEY_ROLES, rolesJSON);
    }  
    
    private void getPermissionTypes(JSONObject root, String language) {
        
        final JSONArray permissionNames = new JSONArray();
        for (String id : getAvailablePermissions()) {
            JSONObject perm = new JSONObject();
            JSONHelper.putValue(perm, KEY_ID, id);
            JSONHelper.putValue(perm, KEY_NAME, permissionsService.getPermissionName(id, language));
            permissionNames.put(perm);
        }
        JSONHelper.putValue(root, JSKEY_PERMISSION_TYPES, permissionNames);
        
    }
    
    private Set<String> getAvailablePermissions() {
        
        Set<String> availablePermissionTypes = new LinkedHashSet <>();
        for (PermissionType type : PermissionType.values()) {
            if (type.isLayerSpecific()) {
                availablePermissionTypes.add(type.name());
            }
        }
        // add any additional permissions
        availablePermissionTypes.addAll(permissionsService.getAdditionalPermissions());
        return availablePermissionTypes;
    }
}

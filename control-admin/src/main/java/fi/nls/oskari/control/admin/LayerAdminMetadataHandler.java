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
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("LayerAdminMetadata")
public class LayerAdminMetadataHandler extends RestActionHandler {
    
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
        } catch (ServiceException ex) {
            throw new ServiceRuntimeException("Exception occured while initializing user service",ex);
        }
        
        try {
            permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Exception occured while initializing permission service",ex);
        }
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireAdminUser();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        try {
            final JSONObject root = new JSONObject();
            JSONHelper.putValue(root, JSKEY_ROLES, getRoles());
            JSONHelper.putValue(root, JSKEY_PERMISSION_TYPES, getPermissionTypes(params.getLocale().getLanguage()));
            ResponseHelper.writeResponse(params, root);
        } catch (Exception e) {
            throw new ActionException("Something went wrong getting roles and permission types from the platform", e);
        }
    }

    private JSONArray getRoles() throws ServiceException, JSONException  {
        final Role[] roles = userService.getRoles();
        final JSONArray rolesJSON = new JSONArray();

        for (Role role : roles) {
            JSONObject external = new JSONObject();
            external.put(JSKEY_ID, role.getId());
            external.put(JSKEY_NAME, role.getName());
            rolesJSON.put(external);
        }
        return rolesJSON;
    }  
    
    private JSONArray getPermissionTypes(String language) {
        
        final JSONArray permissionNames = new JSONArray();
        for (String id : getAvailablePermissions()) {
            JSONObject perm = new JSONObject();
            JSONHelper.putValue(perm, KEY_ID, id);
            JSONHelper.putValue(perm, KEY_NAME, permissionsService.getPermissionName(id, language));
            permissionNames.put(perm);
        }
       return permissionNames;
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

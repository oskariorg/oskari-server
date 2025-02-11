package fi.nls.oskari.control.admin;

import static fi.nls.oskari.control.ActionConstants.KEY_ID;
import static fi.nls.oskari.control.ActionConstants.KEY_NAME;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.capabilities.UpdateCapabilitiesJob;
import org.oskari.maplayer.admin.LayerValidator;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.PermissionType;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import org.oskari.user.Role;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.user.UserJsonHelper;

@OskariActionRoute("LayerAdminMetadata")
public class LayerAdminMetadataHandler extends RestActionHandler {
    
    private UserService userService = null;
    private PermissionService permissionsService = null;

    private String capabilitiesCron = null;

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

    private String getCapabilitiesUpdateCron() {
        if (capabilitiesCron == null) {
            // "oskari.scheduler.job.UpdateCapabilitiesJob.cronLine" in oskari-ext.properties or default from class
            capabilitiesCron = new UpdateCapabilitiesJob().getCronLine();
        }
        return capabilitiesCron;
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireAdminUser();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        try {
            final JSONObject root = new JSONObject();
            JSONHelper.putValue(root, "roles", getRoles());
            JSONHelper.putValue(root, "permissionTypes", getPermissionTypes(params.getLocale().getLanguage()));
            JSONHelper.putValue(root, "layerTypes", getMandatoryFields());
            JSONHelper.putValue(root, "capabilitiesCron", getCapabilitiesUpdateCron());
            JSONHelper.putValue(root, "systemRoles", Role.getSystemRolesAsMap());
            ResponseHelper.writeResponse(params, root);
        } catch (Exception e) {
            throw new ActionException("Something went wrong getting roles and permission types from the platform", e);
        }
    }

    private JSONArray getRoles() throws ServiceException, JSONException  {
        final Role[] roles = userService.getRoles();
        final JSONArray rolesJSON = new JSONArray();
        if (roles != null) {
            for (Role role : roles) {
                rolesJSON.put(UserJsonHelper.toJSON(role));
            }
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

    private Map<String, Set<String>> getMandatoryFields() {
        Map<String, Set<String>> fields = new HashMap<>();
        for (String layertype: LayerValidator.getRecognizedLayerTypes()) {
            fields.put(layertype, LayerValidator.getMandatoryFields(layertype));
        }
        return fields;
    }
}

package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.log.AuditLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.PermissionExternalType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OskariActionRoute("SaveLayerPermission")
public class SaveLayerPermissionHandler extends RestActionHandler {

    private final static Logger log = LogFactory.getLogger(SaveLayerPermissionHandler.class);
    private static String PARAMETER_PERMISSION_DATA = "resource";
    private PermissionService permissionsService;

    public void init() {
        permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        // only accept admins
        params.requireAdminUser();

        final JSONArray resources = parseJSONArray(params.getHttpParam(PARAMETER_PERMISSION_DATA));
        final List<String> layerMappings = new ArrayList<>();

        try {
            for (int i = 0; i < resources.length(); i++) {
                final JSONObject layerPermission = resources.getJSONObject(i);
                final String layerMapping = layerPermission.getString("namespace");
                final Optional<Resource> dbResource = permissionsService.findResource(ResourceType.maplayer, layerMapping);
                if (!dbResource.isPresent()) {
                    throw new ActionParamsException("Resource not found: " + layerMapping);
                }
                Resource resource = dbResource.get();
                final int roleId = Integer.parseInt(layerPermission.getString("roleId"));
                JSONArray perm = layerPermission.getJSONArray("permissions");
                for (int j = 0; j < perm.length(); j++) {
                    JSONObject obj = perm.getJSONObject(j);
                    String permissionId = obj.getString("key");
                    if (!obj.getBoolean("value")) {
                        // permission was REMOVED
                        resource.removePermissionsOfType(permissionId, PermissionExternalType.ROLE, roleId);
                    } else {
                        // permission was GRANTED
                        Permission permission = new Permission();
                        permission.setRoleId(roleId);
                        permission.setType(permissionId);
                        resource.addPermission(permission);
                    }
                }
                permissionsService.saveResource(resource);
                AuditLog.user(params.getClientIp(), params.getUser())
                        .withParam("id", resource.getMapping())
                        .updated(AuditLog.ResourceType.MAPLAYER_PERMISSION);
                layerMappings.add(resource.getMapping());
            }
            // TODO: previously didn't respond at all. Should respond with updated data
            ResponseHelper.writeResponse(params, "success");
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid input");
        } finally {
            log.info("Layer permissions updated by", params.getUser().getScreenname(), "Layers updated:", layerMappings);
        }
    }

    private JSONArray parseJSONArray(final String jsonArray) throws ActionParamsException {
        try {
            final JSONArray resources = new JSONArray(jsonArray);
            log.debug(" permissions JSON ", resources);
            return resources;
        } catch (Exception e) {
            throw new ActionParamsException("Unable to parse param JSON:\n" + jsonArray);
        }
    }

}




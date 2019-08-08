package fi.nls.oskari.control.layer;

import fi.mml.portti.domain.permissions.Permissions;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OskariActionRoute("SaveLayerPermission")
public class SaveLayerPermissionHandler extends RestActionHandler {

    private static String PARAMETER_PERMISSION_DATA = "resource";

    private final static Logger log = LogFactory.getLogger(SaveLayerPermissionHandler.class);
    private PermissionService permissionsService;

    public void init() {
        permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        // only accept admins
        params.requireAdminUser();

        final Permissions permissions = new Permissions();
        permissions.setExternalIdType(Permissions.EXTERNAL_TYPE_ROLE);
        permissions.getUniqueResourceName().setType(Permissions.RESOURCE_TYPE_MAP_LAYER);

        final JSONArray resources = parseJSONArray(params.getHttpParam(PARAMETER_PERMISSION_DATA));
        final List<String> layerMappings = new ArrayList<>();

        try {
            for (int i = 0; i < resources.length(); i++) {
                final JSONObject layerPermission = resources.getJSONObject(i);
                final String layerMapping = layerPermission.getString("namespace") + "+" + layerPermission.getString("resourceName");
                final Optional<Resource> dbResource = permissionsService.findResource(ResourceType.maplayer, layerMapping);
                if (!dbResource.isPresent()) {
                    throw new ActionParamsException("Resource not found: " + layerMapping);
                }
                Resource resource = dbResource.get();
                // clear previous
                resource.setPermissions(null);

                final int roleId = Integer.parseInt(layerPermission.getString("roleId"));
                JSONArray perm = layerPermission.getJSONArray("permissions");
                for (int j = 0; j < perm.length(); j++) {
                	JSONObject obj = perm.getJSONObject(j);
                	if (!obj.getBoolean("value")) {
                	    // permission was not granted
                	    return;
                	}
                	Permission permission = new Permission();
                    permission.setRoleId(roleId);
                    permission.setType(obj.getString("key"));
                    resource.addPermission(permission);
            	}
                permissionsService.saveResource(resource);
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




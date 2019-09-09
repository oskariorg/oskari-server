package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.*;

import java.util.*;

import static fi.nls.oskari.control.ActionConstants.KEY_ID;
import static fi.nls.oskari.control.ActionConstants.KEY_NAME;

/**
 * Configuring additional permission types in oskari-ext.properties
 * <p>
 * permission.types = EDIT_LAYER_CONTENT
 * permission.EDIT_LAYER_CONTENT.name.fi=Muokkaa tasoa
 * permission.EDIT_LAYER_CONTENT.name.en=Edit layer
 */
@OskariActionRoute("GetPermissionsLayerHandlers")
public class GetPermissionsLayerHandlers extends ActionHandler {

    private static String JSON_NAMES_SPACE = "namespace";
    private static String JSON_RESOURCE_NAME = "resourceName";
    private static String JSON_RESOURCE = "resource";
    private OskariLayerService mapLayerService;
    private PermissionService permissionsService;
    private Set<String> availablePermissionTypes;

    @Override
    public void init() {
        super.init();
        mapLayerService = OskariComponentManager.getComponentOfType(OskariLayerService.class);
        permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
        // Just so we don't need to do this on the first request
        availablePermissionTypes = getAvailablePermissions();
    }

    private Set<String> getAvailablePermissions() {
        if (availablePermissionTypes != null) {
            return availablePermissionTypes;
        }

        availablePermissionTypes = new LinkedHashSet <>();
        // add default permissions
        // we could add all but it's more than the UI can handle at the moment and some of them don't make sense
        // to be set per layer (like ADD_MAPLAYER)
        for (PermissionType type : PermissionType.values()) {
            if (type.isLayerSpecific()) {
                availablePermissionTypes.add(type.name());
            }
        }
        // add any additional permissions
        availablePermissionTypes.addAll(permissionsService.getAdditionalPermissions());
        return availablePermissionTypes;
    }

    private PermissionExternalType validateType(String type) throws ActionParamsException {
        if (PermissionExternalType.ROLE.name().equalsIgnoreCase(type)) {
            return PermissionExternalType.ROLE;
        }
        throw new ActionParamsException("Only role-based permissions supported currently");
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        // require admin user
        params.requireAdminUser();

        validateType(params.getRequiredParam("externalType"));
        final int roleId = params.getRequiredParamInt("externalId");


        final JSONArray permissionNames = new JSONArray();
        for (String id : getAvailablePermissions()) {
            JSONObject perm = new JSONObject();
            JSONHelper.putValue(perm, KEY_ID, id);
            JSONHelper.putValue(perm, KEY_NAME, permissionsService.getPermissionName(id, params.getLocale().getLanguage()));
            permissionNames.put(perm);
        }
        final JSONObject root = new JSONObject();
        JSONHelper.putValue(root, "names", permissionNames);

        final List<OskariLayer> layers = mapLayerService.findAll();
        Collections.sort(layers);

        List<Resource> resources = permissionsService.findResourcesByType(ResourceType.maplayer);
        PermissionSet permissions = new PermissionSet(resources);

        for (OskariLayer layer : layers) {
            if (layer.isInternal()) {
                // skip internal layers
                continue;
            }
            try {
                JSONObject realJson = new JSONObject();
                realJson.put(KEY_ID, layer.getId());
                realJson.put(KEY_NAME, layer.getName(PropertyUtil.getDefaultLanguage()));
                realJson.put(JSON_NAMES_SPACE, Integer.toString(layer.getId()));
                realJson.put(JSON_RESOURCE_NAME, Integer.toString(layer.getId()));

                Optional<Resource> layerResource = permissions.get(ResourceType.maplayer, Integer.toString(layer.getId()));
                JSONArray jsonResults = new JSONArray();
                for (String permission : getAvailablePermissions()) {
                    JSONObject layerJson = new JSONObject();
                    layerJson.put(KEY_ID, permission);
                    layerJson.put("allow", layerResource
                            .map(r -> r.hasRolePermission(roleId, permission))
                            .orElse(false));
                    jsonResults.put(layerJson);
                }
                realJson.put("permissions", jsonResults);

                root.append(JSON_RESOURCE, realJson);
            } catch (JSONException e) {
                throw new ActionException("Something is wrong with doPermissionResourcesJson ajax reguest", e);
            }
        }

        ResponseHelper.writeResponse(params, root.toString());
    }
}

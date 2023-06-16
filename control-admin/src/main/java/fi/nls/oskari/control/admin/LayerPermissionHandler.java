package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
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

import static fi.nls.oskari.control.ActionConstants.*;

/**
 * Configuring additional permission types in oskari-ext.properties
 * <p>
 * permission.types = EDIT_LAYER_CONTENT
 * permission.EDIT_LAYER_CONTENT.name.fi=Muokkaa tasoa
 * permission.EDIT_LAYER_CONTENT.name.en=Edit layer
 */
@OskariActionRoute("LayerPermission")
public class LayerPermissionHandler extends AbstractLayerAdminHandler {

    private OskariLayerService mapLayerService;
    private PermissionService permissionsService;
    private Set<String> availablePermissionTypes;

    private static final String KEY_NAMES = "names";
    private static final String KEY_LAYERS = "layers";
    private static final String KEY_PERMISSION = "permission";

    @Override
    public void init() {
        super.init();
        mapLayerService = OskariComponentManager.getComponentOfType(OskariLayerService.class);
        permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
        // Just so we don't need to do this on the first request
        availablePermissionTypes = getAvailablePermissions();
    }

    public void preProcess(ActionParameters params) throws ActionException {
        // require admin user
        params.requireAdminUser();
    }

    /**
     * Returns permissions for all layers for the given role
     *
     * @param params
     * @throws ActionException
     */
    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        int roleId = params.getRequiredParamInt(PARAM_ID);
        String lang = params.getLocale().getLanguage();

        final JSONObject root = new JSONObject();
        JSONHelper.putValue(root, KEY_NAMES, getPermissionNames(lang));
        JSONArray layerPermission = new JSONArray();
        JSONHelper.putValue(root, KEY_LAYERS, layerPermission);

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
                JSONObject layerJSON = new JSONObject();
                layerJSON.put(KEY_ID, layer.getId());
                layerJSON.put(KEY_NAME, layer.getName(PropertyUtil.getDefaultLanguage()));
                layerJSON.put(KEY_PERMISSION, getPermissionsForLayer(permissions, layer.getId(), roleId));
            } catch (JSONException e) {
                throw new ActionException("Something is wrong with doPermissionResourcesJson ajax reguest", e);
            }
        }

        ResponseHelper.writeResponse(params, root.toString());
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        // TODO: basically SaveLayerPermissionHandler, but check if the syntax still makes sense
    }

    private JSONObject getPermissionNames(String lang) {
        final JSONObject permissionNames = new JSONObject();
        for (String id : availablePermissionTypes) {
            JSONHelper.putValue(permissionNames, id, permissionsService.getPermissionName(id, lang));
        }
        return permissionNames;
    }
    private JSONObject getPermissionsForLayer(PermissionSet permissions, int layerId, int roleId) {
        JSONObject permissionJSON = new JSONObject();
        Optional<Resource> layerResource = permissions.get(ResourceType.maplayer, Integer.toString(layerId));
        for (String permission : availablePermissionTypes) {
            boolean hasPermission = layerResource
                    .map(r -> r.hasRolePermission(roleId, permission))
                    .orElse(false);
            JSONHelper.putValue(permissionJSON, permission, hasPermission);
        }
        return permissionJSON;
    }
}

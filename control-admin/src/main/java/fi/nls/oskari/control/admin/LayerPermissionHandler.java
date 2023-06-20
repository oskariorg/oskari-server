package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
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
    private static final String KEY_PERMISSION = "permissions";

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
     * Returns permissions for all layers or a single layer when id-param is used
     *
     * @param params
     * @throws ActionException
     */
    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        int layerId = params.getHttpParam(PARAM_ID, -1);
        String lang = params.getLocale().getLanguage();

        final JSONObject root = new JSONObject();
        JSONHelper.putValue(root, KEY_NAMES, getPermissionNames(lang));
        JSONArray layerPermission = new JSONArray();
        JSONHelper.putValue(root, KEY_LAYERS, layerPermission);

        List<OskariLayer> layers;
        if (layerId != -1) {
            OskariLayer layer = mapLayerService.find(layerId);
            layers = new ArrayList<>(5);
            layers.add(layer);
        } else {
            layers = mapLayerService.findAll();
        }
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
                layerJSON.put(KEY_PERMISSION, getPermissionsForLayer(permissions, layer.getId()));
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

    private Role[] getRoles() {
        try {
            return getUserService().getRoles(Collections.emptyMap());
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Unable to list roles");
        }
    }

    private JSONObject getPermissionNames(String lang) {
        final JSONObject permissionNames = new JSONObject();
        for (String id : availablePermissionTypes) {
            JSONHelper.putValue(permissionNames, id, permissionsService.getPermissionName(id, lang));
        }
        return permissionNames;
    }

    /**
     * {
     *     roleId: {
     *         VIEW_LAYER: true,
     *         PUBLISH: false
     *     },
     *     roleId2: {
     *         VIEW_LAYER: true,
     *         PUBLISH: false
     *     }
     * }
     * @param permissions
     * @param layerId
     * @return
     */
    private JSONObject getPermissionsForLayer(PermissionSet permissions, int layerId) {
        JSONObject rolesJSON = new JSONObject();
        Optional<Resource> layerResource = permissions.get(ResourceType.maplayer, Integer.toString(layerId));

        for (Role role : getRoles()) {
            JSONObject permissionJSON = new JSONObject();
            long roleId = role.getId();
            JSONHelper.putValue(rolesJSON, Long.toString(roleId), permissionJSON);
            for (String permission : availablePermissionTypes) {
                boolean hasPermission = layerResource
                        .map(r -> r.hasRolePermission(roleId, permission))
                        .orElse(false);
                JSONHelper.putValue(permissionJSON, permission, hasPermission);
            }
        }
        return rolesJSON;
    }
}

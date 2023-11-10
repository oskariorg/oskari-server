package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.layer.GetMapLayerGroupsHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
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
import org.oskari.log.AuditLog;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.*;

import java.util.*;
import java.util.stream.Collectors;

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
    private static final String KEY_LAYER_TYPE = "layerType";


    private final static Logger log = LogFactory.getLogger(LayerPermissionHandler.class);

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
            if (layer.isInternal() || layer.getParentId() != -1) {
                // skip internal layers and sublayers
                continue;
            }
            try {
                JSONObject layerJSON = new JSONObject();
                layerJSON.put(KEY_ID, layer.getId());
                layerJSON.put(KEY_NAME, layer.getName(PropertyUtil.getDefaultLanguage()));
                layerJSON.put(KEY_PERMISSION, getPermissionsForLayer(permissions, layer.getId()));
                layerJSON.put(KEY_LAYER_TYPE, layer.getType());
                layerPermission.put(layerJSON);
            } catch (JSONException e) {
                throw new ActionException("Something is wrong with doPermissionResourcesJson ajax reguest", e);
            }
        }

        ResponseHelper.writeResponse(params, root.toString());
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        //only accept admins
        params.requireAdminUser();

        final JSONArray resources = parseJSONArray(params.getHttpParam(KEY_LAYERS));
        final List<String> layerMappings = new ArrayList<>();

        try {
            for (int i = 0; i < resources.length(); i++) {
                final JSONObject layerPermission = resources.getJSONObject(i);
                final String layerMapping = new Integer(layerPermission.getInt("id")).toString();
                final Optional<Resource> dbResource = permissionsService.findResource(ResourceType.maplayer, layerMapping);
                if (!dbResource.isPresent()) {
                    throw new ActionParamsException("Resource not found: " + layerMapping);
                }
                Resource resource = dbResource.get();
                final int roleId = Integer.parseInt(layerPermission.getString("roleId"));
                JSONArray perm = layerPermission.getJSONArray("permissions");
                final List<Permission> resourcePermissions = resource.getPermissions();

                
                for (int n = 0; n < resourcePermissions.size(); n++) {
                    Permission permission = resourcePermissions.get(n);
                    boolean found = false;
                    String type = permission.getType();
                    for (int j = 0; j < perm.length(); j++) {
                        if (perm.getString(j).equals(type)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        // permission was REMOVED
                        resource.removePermissionsOfType(type, PermissionExternalType.ROLE, roleId);
                    }
                }
                for (int j = 0; j < perm.length(); j++) {
                    String permissionType = perm.getString(j);

                    if (!resource.hasRolePermission(roleId, permissionType)) {
                        // permission was GRANTED
                        Permission permission = new Permission();
                        permission.setRoleId(roleId);
                        permission.setType(permissionType);
                        resource.addPermission(permission);
                    }
                }
                permissionsService.saveResource(resource);
                AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", resource.getMapping())
                    .updated(AuditLog.ResourceType.MAPLAYER_PERMISSION);
                layerMappings.add(resource.getMapping());
            }
            ResponseHelper.writeResponse(params, "success");
            flushLayerListCache();
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid input");
        } finally {
            log.info("Layer permissions updated by", params.getUser().getScreenname(), "Layers updated:", layerMappings);
        }
    }

    private void flushLayerListCache() {
        CacheManager.getCache(GetMapLayerGroupsHandler.CACHE_NAME).flush(true);
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
            long roleId = role.getId();
            Set<String> allowedPermissions = availablePermissionTypes.stream()
                    .filter(perm -> layerResource
                            .map(r -> r.hasRolePermission(roleId, perm))
                            .orElse(false))
                    .collect(Collectors.toSet());
            if (!allowedPermissions.isEmpty()) {
                JSONHelper.putValue(rolesJSON, Long.toString(roleId), allowedPermissions);
            }
        }
        return rolesJSON;
    }
}

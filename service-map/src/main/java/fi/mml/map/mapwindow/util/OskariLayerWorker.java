package fi.mml.map.mapwindow.util;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

/**
 * Worker class for rendering json objects from domain objects
 */
public class OskariLayerWorker {

    private static final String NO_PUBLICATION_PERMISSION = "no_publication_permission";
    private static final String PUBLICATION_PERMISSION_OK = "publication_permission_ok";

    private static Logger log = LogFactory.getLogger(OskariLayerWorker.class);

    private static OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    private final static LayerJSONFormatter FORMATTER = new LayerJSONFormatter();

    public static JSONObject getListOfAllMapLayers(final User user, final String lang) {
        long start = System.currentTimeMillis();
        final List<OskariLayer> layers = mapLayerService.findAll();
        log.debug("Layers loaded in", System.currentTimeMillis() - start, "ms");
        final boolean isSecure = false;
        final boolean isPublished = false;
        return getListOfMapLayers(layers, user, lang, isPublished, isSecure);
    }

    /**
     * Gets all the selected map layers
     *
     * @param layerIdList List of selected layer IDs
     * @param user        User
     * @param lang        Language
     * @param isPublished Determines the permission type used for the layers (view/published view)
     * @param isSecure    true to modify urls for easier proxy forwarding/false to keep as is
     * @return JSONObject containing the selected layers
     */
    public static JSONObject getListOfMapLayersById(final List<String> layerIdList, final User user,
                                                    final String lang, final boolean isPublished, final boolean isSecure) {
        final List<OskariLayer> layers = mapLayerService.find(layerIdList);
        return getListOfMapLayers(layers, user, lang, isPublished, isSecure);
    }

    /**
     * Gets all the map layers the user is permitted to view
     *
     * @param user User
     * @param lang Language
     * @param lang        Language
     * @param isPublished Determines the permission type used for the layers (view/published view)
     * @param isSecure    true to modify urls for easier proxy forwarding/false to keep as is
     * @return JSONObject of map layers
     */
    public static JSONObject getListOfMapLayers(final List<OskariLayer> layers, final User user,
                                                final String lang, final boolean isPublished, final boolean isSecure) {

        final String permissionType = getPermissionType(isPublished);
        long start = System.currentTimeMillis();
        final List<String> resources = permissionsService.getResourcesWithGrantedPermissions(
                Permissions.RESOURCE_TYPE_MAP_LAYER, user, permissionType);
        log.debug("View permissions loaded in", System.currentTimeMillis() - start, "ms");

        start = System.currentTimeMillis();
        final Set<String> permissionsList = permissionsService.getPublishPermissions();
        log.debug("Publish permissions loaded in", System.currentTimeMillis() - start, "ms");
        start = System.currentTimeMillis();
        final Set<String> editAccessList = permissionsService.getEditPermissions();
        log.debug("Edit permissions loaded in", System.currentTimeMillis() - start, "ms");

        final JSONArray layersList = new JSONArray();
        start = System.currentTimeMillis();
        // count average/layertype
        for (OskariLayer layer : layers) {
            final String permissionKey = layer.getUrl() + "+" + layer.getName();
            if (!resources.contains(permissionKey)) {
                // not permitted!
                continue;
            }
            try {
                final JSONObject layerJson = FORMATTER.getJSON(layer, lang, isSecure);
                //log.debug("Generated JSON");
                if (layerJson != null) {
                    //log.debug("Generating permissions JSON");
                    JSONObject permissions = getPermissions(user, permissionKey, permissionsList, editAccessList);
                    JSONHelper.putValue(layerJson, "permissions", permissions);
                    //log.debug("Adding layer to list");
                    layersList.put(layerJson);
                }
            }
            catch(Exception ex) {
                log.error(ex);
            }
        }
        log.debug("Created JSON in", System.currentTimeMillis() - start, "ms");
        log.debug("Returning", layersList.length(), "/", layers.size(),"layers");

        final JSONObject result = new JSONObject();
        JSONHelper.putValue(result, "layers", layersList);
        return result;
    }


    /**
     * Create permission information for JSON
     *
     * @param user               Current user
     * @param layerPermissionKey Layer permission key
     * @param permissionsList    List of user permissions
     */
    private static JSONObject getPermissions(final User user, final String layerPermissionKey,
                                             final Set<String> permissionsList, final Set<String> editAccessList) {

        final JSONObject permission = new JSONObject();
        if (user.isAdmin()) {
            JSONHelper.putValue(permission, "edit", true);
            JSONHelper.putValue(permission, "publish", PUBLICATION_PERMISSION_OK);
        } else if (user.isGuest()) {
            JSONHelper.putValue(permission, "publish", NO_PUBLICATION_PERMISSION);
        } else {
            for (Role role : user.getRoles()) {
                if (editAccessList.contains(layerPermissionKey + ":" + role.getId())) {
                    JSONHelper.putValue(permission, "edit", true);
                }
                if (permissionsList.contains(layerPermissionKey + ":" + role.getId())) {
                    JSONHelper.putValue(permission, "publish", PUBLICATION_PERMISSION_OK);
                }
            }
        }
        return permission;
    }

    private static String getPermissionType(final boolean isPublished) {
        if (isPublished) {
            return Permissions.PERMISSION_TYPE_VIEW_PUBLISHED;
        }
        return Permissions.PERMISSION_TYPE_VIEW_LAYER;
    }
}

package fi.mml.map.mapwindow.util;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Worker class for rendering json objects from domain objects
 */
public class OskariLayerWorker {

    public static final String KEY_LAYERS = "layers";

    private static final String NO_PUBLICATION_PERMISSION = "no_publication_permission";
    private static final String PUBLICATION_PERMISSION_OK = "publication_permission_ok";

    private static final String DOWNLOAD_PERMISSION_OK = "download_permission_ok";
    private static final String NO_DOWNLOAD_PERMISSION = "no_download_permission";

    private static Logger log = LogFactory.getLogger(OskariLayerWorker.class);

    private static OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();
    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();

    private final static LayerJSONFormatter FORMATTER = new LayerJSONFormatter();
    
    public static JSONObject getListOfAllMapLayers(final User user, final String lang, final String crs) {
        return getListOfAllMapLayers(user, lang, crs, false);
    }

    public static JSONObject getListOfAllMapLayers(final User user, final String lang) {
        return getListOfAllMapLayers(user, lang, null, false);
    }

    public static JSONObject getListOfAllMapLayers(final User user, final String lang, final String crs, final boolean isSecure) {
        long start = System.currentTimeMillis();
        final List<OskariLayer> layers = mapLayerService.findAll(crs);
        log.debug("Layers loaded in", System.currentTimeMillis() - start, "ms");
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
                                                    final String lang, final String crs, final boolean isPublished, final boolean isSecure) {
        final List<OskariLayer> layers = mapLayerService.find(layerIdList, crs);
        return getListOfMapLayers(layers, user, lang, isPublished, isSecure);
    }

    /**
     * Gets all the map layers the user is permitted to view
     *
     * @param layers      list of layers to render
     * @param user        User
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
        final Set<String> downloadPermissionsList = permissionsService.getDownloadPermissions();
        log.debug("Download permissions loaded in", System.currentTimeMillis() - start, "ms");

        start = System.currentTimeMillis();
        final Set<String> editAccessList = permissionsService.getEditPermissions();
        log.debug("Edit permissions loaded in", System.currentTimeMillis() - start, "ms");

        final Set<String> additionalPermissions = permissionsService.getAdditionalPermissions();
    	log.debug("Loading dynamic permissions ", additionalPermissions);
        final Map<String, List<String>> dynamicPermissions = new HashMap<String, List<String>>();
        for (String permissionId : additionalPermissions) {
            final List<String> permissions = permissionsService
                    .getResourcesWithGrantedPermissions(
                            Permissions.RESOURCE_TYPE_MAP_LAYER, user,
                            permissionId);
            dynamicPermissions.put(permissionId,permissions);
            log.debug("Got " + permissions.size() + " permissions of type " + permissionId);
        }

        final JSONArray layersList = new JSONArray();
        start = System.currentTimeMillis();
        for (OskariLayer layer : layers) {
            final String permissionKey = layer.getType()+ "+" + layer.getUrl() + "+" + layer.getName();
            if (layer.getParentId() == -1 && !resources.contains(permissionKey)) {
                // not permitted if resource NOT found in permissions!
                // sublayers can pass through since their parentId != -1
                continue;
            }
            try {
                final JSONObject layerJson = FORMATTER.getJSON(layer, lang, isSecure);
                //log.debug("Generated JSON");
                if (layerJson != null) {
                    //log.debug("Generating permissions JSON");
                    JSONObject permissions = getPermissions(user, permissionKey, permissionsList, downloadPermissionsList, editAccessList, dynamicPermissions);
                    JSONHelper.putValue(layerJson, "permissions", permissions);
                    if(permissions.optBoolean("edit")) {
                        // has edit rights, alter JSON/add info for admin bundle
                        modifyCommonFieldsForEditing(layerJson, layer);
                    }
                    else {
                        FORMATTER.removeAdminInfo(layerJson);
                    }

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
        JSONHelper.putValue(result, KEY_LAYERS, layersList);
        return result;
    }

    /**
     * Convenience method to get JSON for single layer.
     * TODO: maybe tune the implementation a bit
     * @param layer
     * @param user
     * @param lang
     * @return
     */
    public static JSONObject getMapLayerJSON(final OskariLayer layer, final User user,
                                                final String lang) {

        final List<OskariLayer> list = new ArrayList<OskariLayer>(1);
        list.add(layer);
        final JSONObject obj = OskariLayerWorker.getListOfMapLayers(list, user, lang, false, false);
        JSONArray layers = JSONHelper.getJSONArray(obj, KEY_LAYERS);
        try {
            return layers.getJSONObject(0);
        } catch (Exception e) {
            log.warn("Error creating layer JSON:", obj);
        }
        return null;
    }

    public static void modifyCommonFieldsForEditing(final JSONObject layerJson, final OskariLayer layer) {
        // TODO: should loop sublayers as well if we want admin values for them:
        // * localized names/subtitles (we dont need atm aince they are only shown to admin)
        // * organizationId/inspireId is only relevant to parent layer
        // * xslt might be something we want

        // name
        final JSONObject names = new JSONObject();
        for (Map.Entry<String, String> localization : layer.getNames().entrySet()) {
            JSONHelper.putValue(names, localization.getKey(), localization.getValue());
        }
        JSONHelper.putValue(layerJson, "name", names);

        // subtitle/description
        final JSONObject subtitles = new JSONObject();
        for (Map.Entry<String, String> localization : layer.getTitles().entrySet()) {
            JSONHelper.putValue(subtitles, localization.getKey(), localization.getValue());
        }
        JSONHelper.putValue(layerJson, "subtitle", subtitles);

        FORMATTER.addInfoForAdmin(layerJson, "xslt", layer.getGfiXslt());

        FORMATTER.addInfoForAdmin(layerJson, "username", layer.getUsername());
        FORMATTER.addInfoForAdmin(layerJson, "password", layer.getPassword());
        FORMATTER.addInfoForAdmin(layerJson, "url", layer.getUrl());
        FORMATTER.addInfoForAdmin(layerJson, "capabilities", layer.getCapabilities());

        FORMATTER.addInfoForAdmin(layerJson, "organizationId", layer.getGroupId());

        // for mapping under categories
        if(layer.getInspireTheme() != null) {
            FORMATTER.addInfoForAdmin(layerJson, "inspireId", layer.getInspireTheme().getId());
        }
    }

    public static void transformWKTGeom(final JSONObject layerJSON, final String mapSRS) {

        final String wktWGS84 = layerJSON.optString("geom");
        if(wktWGS84 == null) {
            return;
        }
        // WTK is saved as EPSG:4326 in database
        final String transformed = WKTHelper.transform(wktWGS84, WKTHelper.PROJ_EPSG_4326, mapSRS);
        // value will be removed if transform failed, that's ok since client can't handle it if it's in unknown projection
        JSONHelper.putValue(layerJSON, "geom", transformed);
    }
    
    /**
     * Create permission information for JSON
     *
     * @param user               Current user
     * @param layerPermissionKey Layer permission key
     * @param permissionsList    List of user publish permissions
     * @param downloadPermissionsList    List of user download permissions
     * @param editAccessList     List of user edit permissions
     */
    public static JSONObject getPermissions(final User user, final String layerPermissionKey,
                                             final Set<String> permissionsList, final Set<String> downloadPermissionsList, final Set<String> editAccessList) {
    
    	return getPermissions(user, layerPermissionKey, permissionsList, downloadPermissionsList, editAccessList, null);
    } 

    /**
     * Create permission information for JSON
     *
     * @param user               Current user
     * @param layerPermissionKey Layer permission key
     * @param permissionsList    List of user publish permissions
     * @param downloadPermissionsList    List of user download permissions
     * @param editAccessList     List of user edit permissions
     * @param dynamicPermissions 
     */
    public static JSONObject getPermissions(final User user, final String layerPermissionKey,
                                             final Set<String> permissionsList, final Set<String> downloadPermissionsList, final Set<String> editAccessList, Map<String, List<String>> dynamicPermissions) {

        final JSONObject permission = new JSONObject();
        if (user.isAdmin()) {
            JSONHelper.putValue(permission, "edit", true);
            JSONHelper.putValue(permission, "publish", PUBLICATION_PERMISSION_OK);
            JSONHelper.putValue(permission, "download", DOWNLOAD_PERMISSION_OK);
        } else if (user.isGuest()) {
            JSONHelper.putValue(permission, "publish", NO_PUBLICATION_PERMISSION);
        } else {
            for (Role role : user.getRoles()) {
                if (editAccessList != null && editAccessList.contains(layerPermissionKey + ":" + role.getId())) {
                    JSONHelper.putValue(permission, "edit", true);
                }
                if (permissionsList != null && permissionsList.contains(layerPermissionKey + ":" + role.getId())) {
                    JSONHelper.putValue(permission, "publish", PUBLICATION_PERMISSION_OK);
                }

                if (downloadPermissionsList != null && downloadPermissionsList.contains(layerPermissionKey + ":" + role.getId())) {
                    JSONHelper.putValue(permission, "download", DOWNLOAD_PERMISSION_OK);
                }
            }
        }
		if (dynamicPermissions != null) {
			for (String permissionType : dynamicPermissions.keySet()) {
				List<String> permissionList = dynamicPermissions
						.get(permissionType);
				if (permissionList != null
						&& permissionList.contains(layerPermissionKey)) {
					JSONHelper.putValue(permission, permissionType, true);
				}
			}
		}
        
        return permission;
    }

    public static JSONObject getAllowedPermissions() {
        final JSONObject permissions = new JSONObject();
        JSONHelper.putValue(permissions, "edit", true);
        JSONHelper.putValue(permissions, "publish", PUBLICATION_PERMISSION_OK);
        //this should probably be allowed as well?
        JSONHelper.putValue(permissions, "download", DOWNLOAD_PERMISSION_OK);

        return permissions;
    }

    /**
     * Reorder Oskari layers in to requested order
     * @param layers
     * @param ids  layer ids and externalids
     * @return  reorder layers
     */
    public static List<OskariLayer> reorderLayers(List<OskariLayer> layers, List<String> ids) {

        List<OskariLayer> reLayers = new ArrayList<OskariLayer>();

        for (String id : ids) {
            for (OskariLayer lay : layers) {

                if (Integer.toString(lay.getId()).equals(id) || (lay.getExternalId() != null && lay.getExternalId().equals(id))) {
                    reLayers.add(lay);
                    break;
                }
            }

        }
        return reLayers;
    }

    private static String getPermissionType(final boolean isPublished) {
        if (isPublished) {
            return Permissions.PERMISSION_TYPE_VIEW_PUBLISHED;
        }
        return Permissions.PERMISSION_TYPE_VIEW_LAYER;
    }
}

package fi.nls.oskari.control.layer;

import static fi.nls.oskari.control.ActionConstants.PARAM_LANGUAGE;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.EnvHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.map.mapwindow.util.PermissionCollection;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Get all map layer groups registered in Oskari database
 */
@OskariActionRoute("GetHierarchicalMapLayerGroups")
public class GetMapLayerGroupsHandler extends ActionHandler {

    private static Logger log = LogFactory.getLogger(GetMapLayerGroupsHandler.class);

    private static final String KEY_GROUPS = "groups";
    private OskariLayerService layerService;
    private PermissionsService permissionsService;
    private OskariMapLayerGroupService oskariMapLayerGroupService;

    public void setLayerService(OskariLayerService layerService) {
        this.layerService = layerService;
    }

    public void setPermissionsService(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    public void setOskariMapLayerGroupService(final OskariMapLayerGroupService service) {
        oskariMapLayerGroupService = service;
    }

    @Override
    public void init() {
        // setup services if they haven't been initialized
        if (layerService == null) {
            setLayerService(new OskariLayerServiceIbatisImpl());
        }
        if (permissionsService == null) {
            setPermissionsService(new PermissionsServiceIbatisImpl());
        }
        if (oskariMapLayerGroupService == null) {
            setOskariMapLayerGroupService(new OskariMapLayerGroupServiceIbatisImpl());
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String lang = params.getHttpParam(PARAM_LANGUAGE, params.getLocale().getLanguage());
        final String crs = params.getHttpParam(PARAM_SRS);
        final boolean isSecure = EnvHelper.isSecure(params);
        final boolean isPublished = false;

        final String permissionType = OskariLayerWorker.getPermissionType(isPublished);
        final Set<String> resources = permissionsService.getResourcesWithGrantedPermissions(Permissions.RESOURCE_TYPE_MAP_LAYER, user, permissionType);

        final Map<Integer, OskariLayer> layersById = layerService.findAll().stream()
                .filter(layer -> layer.getParentId() != -1 || resources.contains(OskariLayerWorker.getPermissionKey(layer)))
                .collect(Collectors.toMap(layer -> layer.getId(), layer -> layer));

        final PermissionCollection permissionCollection = OskariLayerWorker.getPermissionCollection(user);

        log.debug("Getting layer groups");
        JSONArray json = getGroupJSON(layersById, user, lang, isSecure, crs, resources, permissionCollection, -1);
        log.debug("Got layer groups");
        ResponseHelper.writeResponse(params, json);
    }


    /**
     * Get group JSON recursive
     *
     * @param parentGroupId parent id
     * @param params   params
     * @return
     * @throws ActionException
     */
    private JSONArray getGroupJSON(final Map<Integer, OskariLayer> layersById, final User user,
            final String lang, final boolean isSecure, final String crs, final Set<String> resources,
            final PermissionCollection permissionCollection, int parentGroupId) throws ActionException {
        try {
            List<MaplayerGroup> layerGroups = oskariMapLayerGroupService.findByParentId(parentGroupId);
            JSONArray json = new JSONArray();
            for (MaplayerGroup group : layerGroups) {
                int groupId = group.getId();

                List<Integer> layerIds = oskariMapLayerGroupService.findMaplayersByGroup(groupId);
                List<OskariLayer> groupsLayers = new ArrayList<>();
                for (Integer layerId : layerIds) {
                    OskariLayer layer = layersById.get(layerId);
                    if (layer != null) {
                        groupsLayers.add(layer);
                    }
                }

                final JSONObject layers = OskariLayerWorker.getListOfMapLayers(groupsLayers, user, lang, isSecure, crs, resources, permissionCollection);
                JSONArray layerList = layers.optJSONArray(OskariLayerWorker.KEY_LAYERS);
                group.setLayers(layerList);

                JSONObject groupJson = group.getAsJSON();

                JSONArray subGroupsJSON = getGroupJSON(layersById, user, lang, isSecure, crs, resources, permissionCollection, groupId);
                groupJson.put(KEY_GROUPS, subGroupsJSON);

                json.put(groupJson);
            }
            return json;
        } catch (JSONException ex) {
            throw new ActionException("Cannot get groupped layerlist", ex);
        }
    }

}

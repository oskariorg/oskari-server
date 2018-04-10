package fi.nls.oskari.control.layer;

import static fi.nls.oskari.control.ActionConstants.PARAM_LANGUAGE;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkServiceMybatisImpl;

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
import fi.nls.oskari.util.ResponseHelper;

/**
 * Get all map layer groups registered in Oskari database
 */
@OskariActionRoute("GetHierarchicalMapLayerGroups")
public class GetMapLayerGroupsHandler extends ActionHandler {

    private static final String KEY_GROUPS = "groups";
    private static final String KEY_LAYERS = "layers";

    private OskariLayerService layerService;
    private PermissionsService permissionsService;
    private OskariMapLayerGroupService groupService;
    private OskariLayerGroupLinkService linkService;

    public void setLayerService(OskariLayerService layerService) {
        this.layerService = layerService;
    }

    public void setPermissionsService(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    public void setGroupService(OskariMapLayerGroupService groupService) {
        this.groupService = groupService;
    }

    public void setLinkService(OskariLayerGroupLinkService linkService) {
        this.linkService = linkService;
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
        if (groupService == null) {
            setGroupService(new OskariMapLayerGroupServiceIbatisImpl());
        }
        if (linkService == null) {
            setLinkService(new OskariLayerGroupLinkServiceMybatisImpl());
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String lang = params.getHttpParam(PARAM_LANGUAGE, params.getLocale().getLanguage());
        final String crs = params.getHttpParam(PARAM_SRS);
        final boolean isSecure = false;
        final boolean isPublished = false;

        final String permissionType = OskariLayerWorker.getPermissionType(isPublished);
        final List<String> resources = permissionsService.getResourcesWithGrantedPermissions(Permissions.RESOURCE_TYPE_MAP_LAYER, user, permissionType);
        final Set<String> resourcesSet = new HashSet<>(resources);

        final List<OskariLayer> layers = layerService.findAll().stream()
                .filter(layer -> layer.isSublayer() || resourcesSet.contains(OskariLayerWorker.getPermissionKey(layer)))
                .collect(Collectors.toList());
        final PermissionCollection permissionCollection = OskariLayerWorker.getPermissionCollection(user);

        final JSONObject response = OskariLayerWorker.getListOfMapLayers(layers, user, lang, isSecure, crs, resources, permissionCollection);

        final List<MaplayerGroup> groups = groupService.findAll();
        final int[] layerIds = layers.stream().mapToInt(OskariLayer::getId).toArray();
        Arrays.sort(layerIds);
        final Map<Integer, List<MaplayerGroup>> groupsByParentId = groups.stream()
                .collect(Collectors.groupingBy(MaplayerGroup::getParentId));

        try {
            response.put(KEY_GROUPS, getGroupJSON(groupsByParentId, layerIds, -1));
            ResponseHelper.writeResponse(params, response);
        } catch (JSONException e) {
            throw new ActionException("Failed to add groups", e);
        }
    }


    /**
     * Get groups recursively
     */
    private JSONArray getGroupJSON(Map<Integer, List<MaplayerGroup>> groupsByParentId, int[] layerIds, int parentGroupId) throws JSONException {
        List<MaplayerGroup> groups = groupsByParentId.get(parentGroupId);
        if (groups == null || groups.isEmpty()) {
            return null;
        }
        JSONArray json = new JSONArray();
        groups.sort(Comparator.comparing(MaplayerGroup::getOrderNumber));
        for (MaplayerGroup group : groups) {
            int groupId = group.getId();
            JSONObject groupAsJson = group.getAsJSON();

            JSONArray subGroups = getGroupJSON(groupsByParentId, layerIds, groupId);
            if (subGroups != null) {
                groupAsJson.put(KEY_GROUPS, subGroups);
            }

            List<Integer> groupsLayerIds = linkService.findByGroupId(groupId).stream()
                    .filter(l -> contains(layerIds, l.getLayerId()))
                    .sorted(Comparator.comparingInt(OskariLayerGroupLink::getOrderNumber))
                    .map(OskariLayerGroupLink::getLayerId)
                    .collect(Collectors.toList());
            if (!groupsLayerIds.isEmpty()) {
                groupAsJson.put(KEY_LAYERS, groupsLayerIds);
            }

            json.put(groupAsJson);
        }
        return json;
    }

    private boolean contains(int[] layerIds, int layerId) {
        return Arrays.binarySearch(layerIds, layerId) >= 0;
    }

}

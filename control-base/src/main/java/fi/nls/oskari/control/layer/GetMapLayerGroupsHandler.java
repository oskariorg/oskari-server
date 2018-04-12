package fi.nls.oskari.control.layer;

import static fi.nls.oskari.control.ActionConstants.PARAM_LANGUAGE;
import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkServiceMybatisImpl;
import fi.nls.oskari.util.EnvHelper;
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
        final boolean isSecure = EnvHelper.isSecure(params);
        final boolean isPublished = false;

        List<OskariLayer> layers = getLayersWithResources(user, isPublished);
        PermissionCollection permissionCollection = OskariLayerWorker.getPermissionCollection(user);

        int[] layerIds = layers.stream().mapToInt(OskariLayer::getId).toArray();
        Arrays.sort(layerIds); // Sort the array, we will use binary search later, which relies on this being sorted

        Map<Integer, List<MaplayerGroup>> groupsByParentId = groupService.findAll().stream()
                .collect(Collectors.groupingBy(MaplayerGroup::getParentId));

        Map<Integer, List<OskariLayerGroupLink>> linksByGroupId = linkService.findAll().stream()
                .collect(Collectors.groupingBy(OskariLayerGroupLink::getGroupId));

        try {
            JSONObject response = OskariLayerWorker.getListOfMapLayers(layers, user, lang, isSecure, crs, permissionCollection);
            response.put(KEY_GROUPS, getGroupJSON(groupsByParentId, linksByGroupId, layerIds, -1));
            ResponseHelper.writeResponse(params, response);
        } catch (JSONException e) {
            throw new ActionException("Failed to add groups", e);
        }
    }

    private List<OskariLayer> getLayersWithResources(User user, boolean isPublished) {
        String permissionType = OskariLayerWorker.getPermissionType(isPublished);
        Set<String> resources = permissionsService.getResourcesWithGrantedPermissions(
                Permissions.RESOURCE_TYPE_MAP_LAYER, user, permissionType);
        return OskariLayerWorker.filterLayersWithResources(layerService.findAll(), resources);
    }

    /**
     * Get groups recursively
     */
    private JSONArray getGroupJSON(final Map<Integer, List<MaplayerGroup>> groupsByParentId,
            final Map<Integer, List<OskariLayerGroupLink>> linksByGroupId,
            final int[] layerIds,
            final int parentGroupId) throws JSONException {
        List<MaplayerGroup> groups = groupsByParentId.get(parentGroupId);
        if (groups == null || groups.isEmpty()) {
            return null;
        }

        JSONArray json = new JSONArray();
        groups.sort(Comparator.comparing(MaplayerGroup::getOrderNumber));
        for (MaplayerGroup group : groups) {
            int groupId = group.getId();
            JSONObject groupAsJson = group.getAsJSON();

            JSONArray subGroups = getGroupJSON(groupsByParentId, linksByGroupId, layerIds, groupId);
            if (subGroups != null) {
                groupAsJson.put(KEY_GROUPS, subGroups);
            }

            List<OskariLayerGroupLink> groupLinks = linksByGroupId.get(groupId);
            if (groupLinks != null && groupLinks.isEmpty()) {
                List<Integer> groupsLayerIds = linksByGroupId.get(groupId).stream()
                    .filter(l -> contains(layerIds, l.getLayerId()))
                    .sorted(Comparator.comparingInt(OskariLayerGroupLink::getOrderNumber))
                    .map(OskariLayerGroupLink::getLayerId)
                    .collect(Collectors.toList());
                if (!groupsLayerIds.isEmpty()) {
                    groupAsJson.put(KEY_LAYERS, groupsLayerIds);
                }
            }

            json.put(groupAsJson);
        }
        return json;
    }

    private boolean contains(int[] layerIds, int layerId) {
        return Arrays.binarySearch(layerIds, layerId) >= 0;
    }

}

package fi.nls.oskari.db;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.DataProviderServiceMybatisImpl;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkServiceMybatisImpl;
import fi.nls.oskari.user.MybatisRoleService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.data.model.MapLayer;
import org.oskari.maplayer.LayerAdminJSONHelper;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;
import org.oskari.permissions.model.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LayerHelper {

    private static final Logger log = LogFactory.getLogger(LayerHelper.class);
    private static final OskariLayerService layerService = new OskariLayerServiceMybatisImpl();
    private static final OskariMapLayerGroupService groupService = new OskariMapLayerGroupServiceIbatisImpl();
    private static final OskariLayerGroupLinkService linkService = new OskariLayerGroupLinkServiceMybatisImpl();
    private static final PermissionService permissionsService = new PermissionServiceMybatisImpl();
    private static final MybatisRoleService roleService = new MybatisRoleService();

    public static int setupLayer(final String layerfile) throws IOException {
        final String jsonStr = IOHelper.readString(DBHandler.getInputStreamFromResource("/json/layers/" + layerfile));
        MapLayer layer = LayerAdminJSONHelper.readJSON(jsonStr);
        // TODO: validate parsed layer?
        final List<OskariLayer> dbLayers = layerService.findByUrlAndName(layer.getUrl(), layer.getName());
        if(!dbLayers.isEmpty()) {
            if(dbLayers.size() > 1) {
                log.warn("Found multiple layers with same url and name. Using first one. Url:", layer.getUrl(), "- name:", layer.getName());
            }
            return dbLayers.get(0).getId();
        } else {
            // layer doesn't exist, insert it
            final OskariLayer oskariLayer = LayerAdminJSONHelper.fromJSON(layer);
            int id = layerService.insert(oskariLayer);
            oskariLayer.setId(id);
            setupLayerPermissions(layer.getRole_permissions(), id);

            Set<String> groups = layer.getGroups();
            if (groups == null) {
                return id;
            }
            for (String groupName : groups) {

                final MaplayerGroup group = groupService.findByName(groupName);
                if (group == null) {
                    log.warn("Didn't find match for group:", groupName);
                } else {
                    linkService.insert(new OskariLayerGroupLink(id, group.getId()));
                }
            }
            return id;
        }
    }

    /*
    "role_permissions": {
        "Guest" : ["VIEW_LAYER"],
        "User" : ["VIEW_LAYER"],
        "Administrator" : ["VIEW_LAYER"]
    }
    */
    private static void setupLayerPermissions(Map<String, Set<String>> permissions, int layerId) {
        // setup rights
        if(permissions == null || permissions.isEmpty()) {
            return;
        }
        final Resource res = new Resource();
        res.setType(ResourceType.maplayer);
        res.setMapping(Integer.toString(layerId));

        for(String roleName : permissions.keySet()) {
            final Role role = roleService.findRoleByName(roleName);
            if(role == null) {
                log.warn("Couldn't find matching role in DB:", roleName, "- Skipping!");
                continue;
            }
            final Set<String> permissionTypes = permissions.get(roleName);
            if(permissionTypes == null) {
                continue;
            }
            for (String type: permissionTypes) {
                final Permission permission = new Permission();
                permission.setExternalType(PermissionExternalType.ROLE);
                permission.setExternalId((int) role.getId());
                permission.setType(type);
                res.addPermission(permission);
            }
        }
        permissionsService.saveResource(res);
    }
}

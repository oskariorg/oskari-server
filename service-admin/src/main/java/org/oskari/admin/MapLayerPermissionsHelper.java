package org.oskari.admin;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.user.MybatisRoleService;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.PermissionExternalType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import java.util.Map;
import java.util.Set;

public class MapLayerPermissionsHelper {

    /*
    "role_permissions": {
        "Guest" : ["VIEW_LAYER"],
        "User" : ["VIEW_LAYER"],
        "Administrator" : ["VIEW_LAYER"]
    }
    */
    public static void setLayerPermissions(int layerId, Map<String, Set<String>> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        final Resource res = new Resource();
        res.setType(ResourceType.maplayer);
        res.setMapping(Integer.toString(layerId));

        MybatisRoleService roleService = getRoleService();
        for (String roleName : permissions.keySet()) {
            final Role role = roleService.findRoleByName(roleName);
            if (role == null) {
                // log.warn("Couldn't find matching role in DB:", roleName, "- Skipping!");
                continue;
            }
            final Set<String> permissionTypes = permissions.get(roleName);
            if (permissionTypes == null) {
                continue;
            }
            for (String type : permissionTypes) {
                final Permission permission = new Permission();
                permission.setExternalType(PermissionExternalType.ROLE);
                permission.setExternalId((int) role.getId());
                permission.setType(type);
                res.addPermission(permission);
            }
        }
        getPermissionService().saveResource(res);
    }

    private static MybatisRoleService getRoleService() {
        return OskariComponentManager.getComponentOfType(MybatisRoleService.class);
    }

    private static PermissionService getPermissionService() {
        return OskariComponentManager.getComponentOfType(PermissionService.class);
    }
}

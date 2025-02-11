package org.oskari.admin;

import org.oskari.user.Role;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.user.MybatisRoleService;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.PermissionExternalType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MapLayerPermissionsHelper {

    /*
    Removes permissions for roles that are referenced in permissions parameter and updates them.
    To remove permissions from a role send an empty array:
    "role_permissions": {
        "Guest" : ["VIEW_LAYER"],
        "User" : ["VIEW_LAYER"],
        "Administrator" : []
    }

    Permissions for any roles that are NOT referenced will be kept as is (allows non-admins to modify).
    */
    public static void setLayerPermissions(int layerId, Map<String, Set<String>> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }

        Resource res = getPermissionService()
                .findResource(ResourceType.maplayer, Integer.toString(layerId))
                .orElse(new Resource());
        res.setType(ResourceType.maplayer);
        res.setMapping(Integer.toString(layerId));
        Set<String> roleNames = permissions.keySet();
        removePermissionsForRoles(res, roleNames);

        MybatisRoleService roleService = getRoleService();
        for (String roleName : roleNames) {
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
                if (type == null || type.trim().isEmpty()) {
                    continue;
                }
                final Permission permission = new Permission();
                permission.setExternalType(PermissionExternalType.ROLE);
                permission.setExternalId((int) role.getId());
                permission.setType(type);
                res.addPermission(permission);
            }
        }
        getPermissionService().saveResource(res);
    }

    /**
     * Removes any existing role based permissions from resource
     * @param resource
     * @param roleNames names of roles that need to be purged
     */
    private static void removePermissionsForRoles(Resource resource, Set<String> roleNames) {
        if (resource == null || roleNames == null) {
            return;
        }
        MybatisRoleService roleService = getRoleService();
        for (String roleName : roleNames) {
            final Role role = roleService.findRoleByName(roleName);
            if (role == null) {
                continue;
            }
            resource.removePermissionsForExternalType(PermissionExternalType.ROLE, (int) role.getId());
        }
    }

    public static void removePermissions(final int layerId) {
        Optional<Resource> res = getPermissionService().findResource(ResourceType.maplayer, Integer.toString(layerId));
        if (res.isPresent()) {
            getPermissionService().deleteResource(res.get());
        }
    }

    private static MybatisRoleService getRoleService() {
        return OskariComponentManager.getComponentOfType(MybatisRoleService.class);
    }

    private static PermissionService getPermissionService() {
        return OskariComponentManager.getComponentOfType(PermissionService.class);
    }
}

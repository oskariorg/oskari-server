package org.oskari.permissions.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;

/**
 * A generic mapping for resource. Reflects DB table oskari_resource.
 */
public class Resource {

    private int id = -1;
    private String  type;
    private String mapping;
    private List<Permission> permissions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type =  type;
    }

    public void setType(ResourceType type) {
        setType(type.name());
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public void setMapping(String namespace, String name) {
        setMapping(namespace + "+" + name);
    }

    public List<Permission> getPermissions() {
        if (permissions == null) {
            return Collections.emptyList();
        }
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(Permission permission) {
        if (permission == null) {
            return;
        }
        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        permissions.add(permission);
    }

    public boolean hasPermission(User user, String permissionType) {
        return hasPermission(user, PermissionType.valueOf(permissionType));
    }

    public boolean hasPermission(User user, PermissionType permissionType) {
        boolean userHasRoleWithId = getPermissions().stream()
                .filter(p -> p.isOfType(permissionType))
                .filter(p -> p.getExternalType() == PermissionExternalType.ROLE)
                .filter(p -> user.hasRoleWithId(p.getExternalId()))
                .findAny()
                .isPresent();

        if (userHasRoleWithId) {
            return true;
        }

        return getPermissions().stream()
                .filter(p -> p.isOfType(permissionType))
                .filter(p -> p.getExternalType() == PermissionExternalType.USER)
                .filter(p -> p.getExternalId() == user.getId())
                .findAny()
                .isPresent();
    }

    public boolean hasPermission(Role role, PermissionType permissionType) {
        return getPermissions().stream()
                .filter(p -> p.isOfType(permissionType))
                .filter(p -> p.getExternalType() == PermissionExternalType.ROLE)
                .filter(p -> p.getExternalId() == role.getId())
                .findAny()
                .isPresent();
    }

    public void removePermissionsOfType(String permissionType) {
        removePermissionsOfType(PermissionType.valueOf(permissionType));
    }

    public void removePermissionsOfType(PermissionType permissionType) {
        getPermissions().removeIf(p -> p.isOfType(permissionType));
    }
}

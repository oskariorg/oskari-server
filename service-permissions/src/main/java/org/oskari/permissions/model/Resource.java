package org.oskari.permissions.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            // mybatis calls get to fill in the results to
            // -> we can't use Collections.emptyList() here
            permissions = new ArrayList<>();
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

    public Set<String> getPermissionTypes() {
        return getPermissions().stream()
                .map(p -> p.getType())
                .collect(Collectors.toSet());
    }

    public boolean hasPermission(User user, PermissionType permissionType) {
        return hasPermission(user, permissionType.name());
    }

    public boolean hasPermission(User user, String permissionType) {
        if (user == null) {
            return false;
        }
        boolean userHasRoleWithId = getPermissions().stream()
                .filter(p -> p.isOfType(permissionType))
                .filter(p -> p.getExternalType() == PermissionExternalType.ROLE)
                .filter(p -> user.hasRoleWithId(p.getExternalId()))
                .findAny()
                .isPresent();

        if (userHasRoleWithId) {
            return true;
        } else if(user.isGuest()) {
            return false;
        }

        return getPermissions().stream()
                .filter(p -> p.isOfType(permissionType))
                .filter(p -> p.getExternalType() == PermissionExternalType.USER)
                .filter(p -> p.getExternalId() == user.getId())
                .findAny()
                .isPresent();
    }

    public boolean hasPermission(Role role, PermissionType permissionType) {
        return hasPermission(role, permissionType.name());
    }
    public boolean hasPermission(Role role, String permissionType) {
        if (role == null) {
            return false;
        }
        return hasRolePermission(role.getId(), permissionType);
    }

    public boolean hasRolePermission(long roleId, String permissionType) {
        return getPermissions().stream()
                .filter(p -> p.isOfType(permissionType))
                .filter(p -> p.getExternalType() == PermissionExternalType.ROLE)
                .filter(p -> p.getExternalId() == roleId)
                .findAny()
                .isPresent();
    }

    public void removePermissionsOfType(PermissionType permissionType, PermissionExternalType idType, int externalId) {
        removePermissionsOfType(permissionType.name(), idType, externalId);
    }

    public void removePermissionsOfType(String permissionType, PermissionExternalType idType, int externalId) {
        getPermissions().removeIf(p -> p.isOfType(permissionType) && p.getExternalType().equals(idType) && p.getExternalId() == externalId);
    }

    public void removePermissionsForExternalType(PermissionExternalType idType, int externalId) {
        getPermissions().removeIf(p -> p.getExternalType().equals(idType) && p.getExternalId() == externalId);
    }

    public void removePermissionsFromAllUsers(String permissionType) {
        getPermissions().removeIf(p -> p.isOfType(permissionType));
    }

    public boolean isOfType(ResourceType type) {
        return isOfType(type.name());
    }

    public boolean isOfType(String permissionType) {
        return type.equals(permissionType);
    }
}

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

    public enum Type {
        maplayer,
        analysislayer,
        layerclass,
        myplaces;
    }

    private int id = -1;
    private Type type;
    private int mapping;
    private List<Permission> permissions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type.name();
    }

    public void setType(String type) {
        setType(Type.valueOf(type));
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getMapping() {
        return Integer.toString(mapping);
    }

    public void setMapping(String type, String mapping) {
        setMapping(mapping);
    }

    public void setMapping(String mapping) {
        setMapping(Integer.parseInt(mapping));
    }

    public void setMapping(int mapping) {
        this.mapping = mapping;
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
            permissions = new ArrayList<>();
        }
        permissions.add(permission);
    }

    public boolean hasPermission(User user, String permissionType) {
        return hasPermission(user, Permission.Type.valueOf(permissionType));
    }

    public boolean hasPermission(User user, Permission.Type permissionType) {
        boolean userHasRoleWithId = permissions.stream()
                .filter(p -> p.getExternalType() == Permission.ExternalType.ROLE)
                .filter(p -> p.getType() == permissionType)
                .filter(p -> user.hasRoleWithId(p.getExternalId()))
                .findAny()
                .isPresent();

        if (userHasRoleWithId) {
            return true;
        }

        return permissions.stream()
                .filter(p -> p.getExternalType() == Permission.ExternalType.USER)
                .filter(p -> p.getType() == permissionType)
                .filter(p -> p.getExternalId() == user.getId())
                .findAny()
                .isPresent();
    }

    public boolean hasPermission(Role role, Permission.Type permissionType) {
        return permissions.stream()
                .filter(p -> p.getType() == permissionType)
                .filter(p -> p.getExternalType() == Permission.ExternalType.ROLE)
                .filter(p -> p.getExternalId() == role.getId())
                .findAny()
                .isPresent();
    }

    public void removePermissionsOfType(String permissionType) {
        removePermissionsOfType(Permission.Type.valueOf(permissionType));
    }

    public void removePermissionsOfType(Permission.Type permissionType) {
        permissions.removeIf(p -> p.getType() == permissionType);
    }
}

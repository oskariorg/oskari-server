package fi.nls.oskari.permission.domain;

import fi.mml.portti.domain.permissions.Permissions;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.ConversionHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic mapping for resource. Reflects DB table oskari_resource.
 */
public class Resource {
    final private List<Permission> permissions = new ArrayList<>();
    private long id = -1;
    private String mapping;
    private String type;

    public boolean hasPermission(final User user, final String permissionType) {
        for(Permission perm: getPermissions()) {
            if(!perm.isOfType(permissionType)) {
                continue;
            }
            if(perm.getExternalType().equals(Permissions.EXTERNAL_TYPE_ROLE)) {
                if(user.hasRoleWithId(ConversionHelper.getInt(perm.getExternalId(), -1))) {
                    return true;
                }
            }
            else if(perm.getExternalType().equals(Permissions.EXTERNAL_TYPE_USER)) {
                final long userID = ConversionHelper.getLong(perm.getExternalId(), -1);
                if(userID == user.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasPermission(final Role role, final String permissionType) {
        for(Permission perm: getPermissions()) {
            if(!perm.isOfType(permissionType)) {
                continue;
            }
            if(perm.getExternalType().equals(Permissions.EXTERNAL_TYPE_ROLE)) {
                if(role.getId() == ConversionHelper.getInt(perm.getExternalId(), -1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removePermissionsOfType(final String permissionType) {
        final List<Permission> toRemove = new ArrayList<Permission>();
        for(Permission perm: getPermissions()) {
            if(perm.isOfType(permissionType)) {
                toRemove.add(perm);
            }
        }
        for(Permission perm : toRemove) {
            getPermissions().remove(perm);
        }
    }


    public List<Permission> getPermissions() {
        return permissions;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public void setMapping(String namespace, String name) {
        this.mapping = namespace + "+" + name;
    }

    public void addPermission(Permission permission) {
        if(permission != null) {
            permissions.add(permission);
        }
    }
}
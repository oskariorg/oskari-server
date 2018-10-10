package org.oskari.permissions.model;

/**
 * A permission mapped to a resource. Reflects DB table oskari_permission.
 */
public class Permission {

    private int id = -1;
    private PermissionType type;
    private PermissionExternalType externalType;
    private int externalId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PermissionType getType() {
        return type;
    }

    public void setType(String type) {
        setType(PermissionType.valueOf(type));
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    public PermissionExternalType getExternalType() {
        return externalType;
    }

    public void setExternalType(String externalType) {
        setExternalType(PermissionExternalType.valueOf(externalType));
    }

    public void setExternalType(PermissionExternalType externalType) {
        this.externalType = externalType;
    }

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }

    public void setExternalId(String externalId) {
        setExternalId(Integer.parseInt(externalId));
    }

    public boolean isOfType(String permissionType) {
        return isOfType(PermissionType.valueOf(permissionType));
    }

    public boolean isOfType(PermissionType permissionType) {
        return type == permissionType;
    }

    /**
     * Copies permission data without id to be used for another resource etc
     * @return
     */
    public Permission clonePermission() {
        Permission p = new Permission();
        p.setType(getType());
        p.setExternalType(getExternalType());
        p.setExternalId(getExternalId());
        return p;
    }
}

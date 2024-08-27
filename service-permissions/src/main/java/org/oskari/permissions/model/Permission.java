package org.oskari.permissions.model;

/**
 * A permission mapped to a resource. Reflects DB table oskari_resource_permission.
 */
public class Permission {

    private int id = -1;
    private String type;
    private PermissionExternalType externalType;
    private int externalId;

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
        this.type = type;
    }

    public void setType(PermissionType type) {
        setType(type.name());
    }

    public PermissionExternalType getExternalType() {
        return externalType;
    }

    public void setExternalType(String externalType) {
        setExternalType(PermissionExternalType.valueOf(externalType));
    }

    public void setExternalTypeMybatis(String externalType) {
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

    public void setExternalIdMybatis(String externalId) {
        setExternalId(externalId);
    }


    public void setRoleId(int roleId) {
        setExternalType(PermissionExternalType.ROLE);
        setExternalId(roleId);
    }

    public void setUserId(int userId) {
        setExternalType(PermissionExternalType.USER);
        setExternalId(userId);
    }

    public boolean isOfType(String permissionType) {
        return type.equals(permissionType);
    }

    public boolean isOfType(PermissionType permissionType) {
        return isOfType(permissionType.name());
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

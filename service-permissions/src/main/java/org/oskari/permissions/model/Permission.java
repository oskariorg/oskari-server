package org.oskari.permissions.model;

/**
 * A permission mapped to a resource. Reflects DB table oskari_permission.
 */
public class Permission {

    public enum Type {
        PUBLISH,
        VIEW_LAYER,
        EDIT_LAYER,
        EDIT_LAYER_CONTENT,
        VIEW_PUBLISHED,
        DOWNLOAD,
        EXECUTE;
    }

    public enum ExternalType {
        ROLE,
        USER;
    }

    private int id = -1;
    private Type type;
    private ExternalType externalType;
    private int externalId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(String type) {
        setType(Type.valueOf(type));
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ExternalType getExternalType() {
        return externalType;
    }

    public void setExternalType(String externalType) {
        setExternalType(ExternalType.valueOf(externalType));
    }

    public void setExternalType(ExternalType externalType) {
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
        return isOfType(Type.valueOf(permissionType));
    }

    public boolean isOfType(Type permissionType) {
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

package fi.nls.oskari.permission.domain;

/**
 * A permission mapped to a resource. Reflects DB table oskari_permission.
 */
public class Permission {
    private String type; // VIEW/PUBLISH/VIEW_PUBLISHED
    private String externalId; // id for external type
    private String externalType; // [ROLE | USER]'
    private long id = -1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalType() {
        return externalType;
    }

    public void setExternalType(String externalType) {
        this.externalType = externalType;
    }

    public boolean isOfType(final String type) {
        if(type == null) {
            return false;
        }
        return type.equals(getType());
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

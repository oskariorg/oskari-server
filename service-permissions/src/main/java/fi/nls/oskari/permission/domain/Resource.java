package fi.nls.oskari.permission.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic mapping for resource. Reflects DB table oskari_resource.
 */
public class Resource {
    final private List<Permission> permissions = new ArrayList<Permission>();
    private long id = -1;
    private String mapping;
    private String type;

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
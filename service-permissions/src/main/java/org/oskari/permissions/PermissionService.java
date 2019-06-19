package org.oskari.permissions;

import java.util.Optional;
import java.util.Set;

import fi.nls.oskari.domain.User;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import fi.nls.oskari.service.OskariComponent;

public abstract class PermissionService extends OskariComponent {

    public abstract Optional<Resource> findResource(int id);
    public abstract Optional<Resource> findResource(ResourceType type, String mapping);
    public abstract Optional<Resource> findResource(String type, String mapping);


    public abstract Set<String>
        getResourcesWithGrantedPermissions(String resourceType, User user, String permissionsType);

    public Set<String>
        getResourcesWithGrantedPermissions(ResourceType resourceType, User user, PermissionType permissionType) {
        return getResourcesWithGrantedPermissions(resourceType.name(), user, permissionType.name());
    }

    public abstract void insertResource(Resource resource);
    public abstract void saveResource(Resource resource);
    public abstract void deleteResource(Resource resource);

}

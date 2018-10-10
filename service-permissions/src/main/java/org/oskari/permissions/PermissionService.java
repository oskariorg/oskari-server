package org.oskari.permissions;

import java.util.Optional;

import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import fi.nls.oskari.service.OskariComponent;

public abstract class PermissionService extends OskariComponent {

    public abstract Optional<Resource> findResource(int id);
    public abstract Optional<Resource> findResource(ResourceType type, int mapping);

    public abstract void insertResource(Resource resource);
    public abstract void deleteResource(Resource resource);

}

package org.oskari.permissions;

import java.util.Optional;

import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.OskariComponent;

public abstract class PermissionService extends OskariComponent {

    public abstract Optional<Resource> findResource(int id);
    public abstract Optional<Resource> findResource(Resource.Type type, int mapping);

    public abstract void insertResource(Resource resource);
    public abstract void deleteResource(Resource resource);

}

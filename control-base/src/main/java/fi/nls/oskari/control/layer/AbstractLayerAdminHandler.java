package fi.nls.oskari.control.layer;

import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponentManager;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.OskariLayerResource;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.ResourceType;

public abstract class AbstractLayerAdminHandler extends RestActionHandler {


    private PermissionService permissionsService;

    public void init() {
        permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
    }

    protected PermissionService getPermissionsService() {
        return permissionsService;
    }

    protected boolean userHasEditPermission(User user, OskariLayer layer) {
        return user.isAdmin() || permissionsService.findResource(ResourceType.maplayer, new OskariLayerResource(layer).getMapping())
                .filter(r -> r.hasPermission(user, PermissionType.EDIT_LAYER)).isPresent();
    }

    protected boolean userHasAddPermission(User user) {
        return user.isAdmin() || permissionsService.findResource(ResourceType.functionality, "generic-functionality")
                .filter(r -> r.hasPermission(user, PermissionType.ADD_MAPLAYER)).isPresent();
    }

}

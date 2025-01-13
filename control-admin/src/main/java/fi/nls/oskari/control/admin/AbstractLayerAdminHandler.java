package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractLayerAdminHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger(AbstractLayerAdminHandler.class);
    private PermissionService permissionsService;
    private UserService userService;

    private Set<String> availablePermissionTypes;

    public void init() {
        permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
        availablePermissionTypes = getAvailablePermissions();
        try {
            userService = UserService.getInstance();
        } catch (ServiceException se) {
            log.error(se, "Unable to initialize User service!");
        }
    }

    protected UserService getUserService() throws ServiceException {
        if (userService == null) {
            userService = UserService.getInstance();
        }
        return userService;
    }

    protected boolean userHasEditPermission(User user, OskariLayer layer) {
        return user.isAdmin() || permissionsService.findResource(ResourceType.maplayer, Integer.toString(layer.getId()))
                .filter(r -> r.hasPermission(user, PermissionType.EDIT_LAYER)).isPresent();
    }

    protected boolean userHasAddPermission(User user) {
        return user.isAdmin() || permissionsService.findResource(ResourceType.functionality, PermissionService.GENERIC_FUNCTIONALITY)
                .filter(r -> r.hasPermission(user, PermissionType.ADD_MAPLAYER)).isPresent();
    }

    protected Map<Role, Set<String>> getPermissionsGroupByRole (User user, OskariLayer layer) throws ServiceException {
        Map<Role, Set<String>> permissions = new HashMap<>();
        String key = Integer.toString(layer.getId());
        Resource res = permissionsService.findResource(ResourceType.maplayer, key)
                .orElseThrow(()-> new ServiceException("Can't find permissions for maplayer with mapping key: " + key));

        for (Role role : getAvailableRoles(user)) {
            Set<String> permissionsForRole = new HashSet<>();
            for (String permission : getAvailablePermissions()){
                if (res.hasPermission(role, permission)) {
                    permissionsForRole.add(permission);
                }
            }
            permissions.put(role, permissionsForRole);
        }
        return permissions;
    }
    private Set<Role> getAvailableRoles(User user) throws ServiceException {
        Set <Role> roles = user.getRoles();
        if (user.isAdmin()){
            roles = Arrays.stream(getUserService().getRoles()).collect(Collectors.toSet());
        } else {
            roles.addAll(getUserService().getGuestUser().getRoles());
        }
        return roles;
    }
    protected Set<String> getAvailablePermissions() {
        if (availablePermissionTypes != null) {
            return availablePermissionTypes;
        }
        availablePermissionTypes = new LinkedHashSet<>();
        // add default permissions
        // we could add all but it's more than the UI can handle at the moment and some of them don't make sense
        // to be set per layer (like ADD_MAPLAYER)
        for (PermissionType type : PermissionType.values()) {
            if (type.isLayerSpecific()) {
                availablePermissionTypes.add(type.name());
            }
        }
        // add any additional permissions
        availablePermissionTypes.addAll(permissionsService.getAdditionalPermissions());
        return availablePermissionTypes;
    }

}

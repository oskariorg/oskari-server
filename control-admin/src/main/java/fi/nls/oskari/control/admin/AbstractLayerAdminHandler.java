package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;
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

    private String[] capabilitiesRoles;
    private Set<String> availablePermissionTypes;



    public void init() {
        capabilitiesRoles = PropertyUtil.getCommaSeparatedList("actionhandler.GetWSCapabilitiesHandler.roles");
        permissionsService = OskariComponentManager.getComponentOfType(PermissionService.class);
        availablePermissionTypes = getAvailablePermissions();

    }

    protected PermissionService getPermissionsService() {
        return permissionsService;
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
        return user.isAdmin() || permissionsService.findResource(ResourceType.functionality, "generic-functionality")
                .filter(r -> r.hasPermission(user, PermissionType.ADD_MAPLAYER)).isPresent();
    }
    protected boolean userHasCapabilitiesPermission(User user) {
        return user.hasAnyRoleIn(capabilitiesRoles) || userHasAddPermission(user);
    }
    protected int getRoleId(String name) throws ServiceException {
        final Role role = getUserService().getRoleByName(name);
        return (int) role.getId();

    }
    protected JSONObject getPermissionsForLayer (User user, OskariLayer layer) throws ActionException, ServiceException {
        if(!userHasEditPermission(user, layer)) throw new ActionDeniedException("User doesn't have edit permission for layer: " + layer.getId());
        JSONObject permissions = new JSONObject();
        String key = Integer.toString(layer.getId());
        Resource res = permissionsService.findResource(ResourceType.maplayer, key).orElseThrow(()-> new ServiceException("Can't find permissions for maplayer with mapping key: " + key));

        for (Role role : getAvailableRoles(user)) {
            JSONArray permissionsForRole = new JSONArray();
            for (String permission : getAvailablePermissions()){
                if (res.hasPermission(role, permission)) {
                    permissionsForRole.put(permission);
                }
            }
            JSONHelper.put(permissions, role.getName(), permissionsForRole);
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
    private Set<String> getAvailablePermissions() {
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
    protected JSONObject getPermissionTemplateJson (User user) {
        try {
            Set<Role> roles = getAvailableRoles(user);
            Optional<Role> guestRole = getUserService().getGuestUser().getRoles().stream().findFirst();
            long guestId = guestRole.isPresent() ? guestRole.get().getId() : -1L;
            long adminId = user.isAdmin() ? Role.getAdminRole().getId() : -1L;
            JSONObject permissions = new JSONObject();
            for (Role role : roles) {
                long id = role.getId();
                if (id == guestId) {
                    JSONHelper.put(permissions, role.getName(), new JSONArray(Collections.singletonList(PermissionType.VIEW_PUBLISHED)));
                } else if (id == adminId) {
                    JSONHelper.put(permissions, role.getName(), new JSONArray(getAvailablePermissions()));
                } else {
                    JSONHelper.put(permissions, role.getName(), new JSONArray());
                }
            }
            return permissions;
        }catch (ServiceException e) {
            log.warn("Failed to create permission template", e);
            return new JSONObject();
        }
    }


}

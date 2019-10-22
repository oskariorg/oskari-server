package org.oskari.permissions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import fi.nls.oskari.service.OskariComponent;

public abstract class PermissionService extends OskariComponent {
    public static final String GENERIC_FUNCTIONALITY = "generic-functionality";

    private Set<String> DYNAMIC_PERMISSIONS;

    public PermissionService() {
        // add any additional permissions
        DYNAMIC_PERMISSIONS = ConversionHelper.asSet(PropertyUtil.getCommaSeparatedList("permission.types"));
    }
    /**
     * Configure additional permissions with oskari-ext.properties:
     *
     *   permission.types = EDIT_LAYER_CONTENT
     *   permission.EDIT_LAYER_CONTENT.name.fi=Muokkaa tasoa
     *   permission.EDIT_LAYER_CONTENT.name.en=Edit layer
     * @return
     */
    public Set<String> getAdditionalPermissions() {
        return DYNAMIC_PERMISSIONS;
    }

    /**
     * Names can be configured as instructed in getAdditionalPermissions() or with
     *   permission.EDIT_LAYER_CONTENT.name = Name for all languages
     *
     * @param permissionId
     * @param lang
     * @return
     */
    public String getPermissionName(String permissionId, String lang) {
        final String property = "permission." + permissionId + ".name";
        final Object obj = PropertyUtil.getLocalizableProperty(property, permissionId);
        if(obj instanceof String) {
            return (String) obj;
        }
        else if(obj instanceof Map) {
            String tmp = (String)((Map) obj).get(lang);
            if(tmp != null) {
                return tmp;
            }
            // TODO: Should we try other languages?
        }
        return permissionId;
    }

    public abstract Optional<Resource> findResource(int id);
    public abstract Optional<Resource> findResource(ResourceType type, String mapping);
    public abstract Optional<Resource> findResource(String type, String mapping);
    public abstract List<Resource> findResourcesByUser(User user, ResourceType type);
    public abstract List<Resource> findResourcesByType(ResourceType type);


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

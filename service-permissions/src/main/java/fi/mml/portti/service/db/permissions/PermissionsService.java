package fi.mml.portti.service.db.permissions;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.domain.permissions.UniqueResourceName;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PermissionsService extends BaseService<Permissions> {

	/**
	 * Returns a set of permission keys that are configured as additional permissions
	 * @return
     */
	Set<String> getAdditionalPermissions();

	/**
	 * Returns an UI name for the permission
	 * @param permissionId
	 * @param lang
     * @return
     */
	String getPermissionName(String permissionId, String lang);
	/**
	 * Insert permissions.
	 * @param uniqueResourceName
	 * @param externalId
	 * @param externalIdType USER or ROLE
	 * @param permissionsType permissions type such as VIEW or PUBLISH
	 */
	public void insertPermissions(
			UniqueResourceName uniqueResourceName, String externalId, String externalIdType, String permissionsType);
	
	/**
	 * Return resources for which permissions have been granted to the given user or role. 
	 * This method returns resources for which:
	 * <ul>
	 * <li>permissions have been granted to user</li>
	 * <li>permissions have been granted to roles where the user belongs</li>
	 * </ul>
	 * 
	 * @param resourceType resource type such as map layer
	 * @param user current user
	 * @param permissionsType permissions type such as VIEW or PUBLISH
	 * @return list of String objects of format namespace+resourceName
	 */
	public List<String> getResourcesWithGrantedPermissions(
			String resourceType, 
			User user,
			String permissionsType);
	
	/**
	 * Return resources for which permissions have been granted to the given user or role.
	 * 
	 * <p>If externalType is USER, this method returns resources for which permissions 
	 * have been granted to user. NOTE! This method does not return resources for which permissions 
	 * have been granted to roles where the user belongs.</p>
	 * 
	 * <p>If externalType is ROLE, this method returns resources for which permissions 
	 * have been granted to role.<p>
	 * 
	 * @param resourceType resource type such as map layer
	 * @param externalId
	 * @param externalIdType
	 * @param permissionsType permissions type such as VIEW or PUBLISH
	 * @return list of String objects of format namespace+resourceName
	 */
	public List<String> getResourcesWithGrantedPermissions(
			String resourceType, 
			String externalId,
			String externalIdType,
			String permissionsType);
	
	/**
	 * Return resource matching type and mapping.
	 * @param type
     * @param mapping
	 * @return resource with permissions
	 */
	public Resource getResource(final String type, final String mapping);

    /**
     * Deletes resource and any permissions bound to it
     * @param resource
     */
    public void deleteResource(final Resource resource);
    /**
     * Return resource matching id (if not -1) or type and mapping.
     * @param resource
     * @return resource with permissions
     */
    public Resource findResource(final Resource resource);
    /**
     * Return resource matching type and mapping.
     *
     * @param resource including permissions to be persisted
     * @return resource id
     */
    public Resource saveResourcePermissions(final Resource resource);
	
	/**
	 * Delete permissions.
	 * 
	 * @param uniqueResourceName
	 * @param externalId
	 * @param externalIdType
	 * @param permissionsType
	 */
	public void deletePermissions(
			UniqueResourceName uniqueResourceName, String externalId, String externalIdType, String permissionsType);
	
	
	
	public Set<String> getPublishPermissions();
	public Set<String> getPublishPermissions(String resourceType);

	public Set<String> getDownloadPermissions();
	public Set<String> getDownloadPermissions(String resourceType);

    public Set<String> getEditPermissions();

    public Map<Long, List<Permissions>> getPermissionsForLayers(List<Long> layeridList, String permissionsType);
    public Map<Long, List<Permissions>> getPermissionsForBaseLayers(List<Long> layeridList, String permissionsType);

    public boolean permissionGrantedForRolesOrUser(long[] roleIdList, long userId, List<Permissions> permissions, String permissionsType);
    public boolean permissionGrantedForRolesOrUser(User user, List<Permissions> permissions, String permissionsType);
    
    public List<Map<String,Object>> getListOfMaplayerIdsForViewPermissionByUser(User user, boolean isViewPublished);
//    public List<Long> getListOfMaplayerIdsForViewPermissionByUser(User user);
    
    public boolean hasViewPermissionForLayerByLayerId(User user, long layerId);

    public boolean hasEditPermissionForLayerByLayerId(User user, long layerId);

    public boolean hasAddLayerPermission(User user);
    
	
}

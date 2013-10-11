package fi.mml.portti.service.db.permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.domain.permissions.UniqueResourceName;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.db.BaseService;

public interface PermissionsService extends BaseService<Permissions> {
	
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
	 * Return USER or ROLE permissions of resource.
	 * 
	 * @param uniqueResourceName
	 * @param externalIdType USER or ROLE
	 * @return
	 */
	public List<Permissions> getResourcePermissions(UniqueResourceName uniqueResourceName, String externalIdType);
	
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

    public Map<Long, List<Permissions>> getPermissionsForLayers(List<Long> layeridList, String permissionsType);
    public Map<Long, List<Permissions>> getPermissionsForBaseLayers(List<Long> layeridList, String permissionsType);

    public boolean permissionGrantedForRolesOrUser(long[] roleIdList, long userId, List<Permissions> permissions, String permissionsType);
    public boolean permissionGrantedForRolesOrUser(User user, List<Permissions> permissions, String permissionsType);
    
    public List<Map<String,Object>> getListOfMaplayerIdsForViewPermissionByUser(User user);
//    public List<Long> getListOfMaplayerIdsForViewPermissionByUser(User user);
    
    public boolean hasViewPermissionForLayerByLayerId(User user, long layerId);
    
    
	
}

package fi.mml.portti.service.db.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.domain.permissions.UniqueResourceName;
import fi.mml.portti.domain.permissions.WFSLayerPermissionsStore;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

public class PermissionsServiceIbatisImpl extends BaseIbatisService<Permissions> implements PermissionsService {		
	
	/** Our logger */
	private static Logger log = LogFactory.getLogger(PermissionsServiceIbatisImpl.class);
	
	@Override
	protected String getNameSpace() {
		return "Permissions";
	}
	
	public void insertPermissions(
			UniqueResourceName uniqueResourceName, String externalId, String externalIdType, String permissionsType) {
		
		// check if row already exists in the RESOURCE_USER table
		Map<String, String> parameterResourceUsers = new HashMap<String, String>();
		parameterResourceUsers.put("resourceName", uniqueResourceName.getName());
		parameterResourceUsers.put("resourceNamespace", uniqueResourceName.getNamespace());
		parameterResourceUsers.put("resourceType", uniqueResourceName.getType());		
		parameterResourceUsers.put("externalId", externalId);
		parameterResourceUsers.put("externalIdType", externalIdType);
		
		int resourceUserId = -1;
		Integer resourceUserIdInteger = queryForObject(getNameSpace() + ".findResourceUserId", parameterResourceUsers);
		
		if (resourceUserIdInteger != null) {
			resourceUserId = resourceUserIdInteger;
		}
		
		// if row does not exist, insert into PORTTI_RESOURCE_USER table
		Permissions p = new Permissions();
		p.setUniqueResourceName(uniqueResourceName);
		p.setExternalId(externalId);
		p.setExternalIdType(externalIdType);		
		
		if (resourceUserId <= 0) {
			resourceUserId = insert(p);
		}		
		
		// insert into PORTTI_PERMISSIONS table
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("resourceUserId", new Integer(resourceUserId));
		paramMap.put("permissionsType", permissionsType);
		
		insert(getNameSpace() + ".insertPermissions", paramMap);

		WFSLayerPermissionsStore.destroyAll();
	}
	
	public List<String> getResourcesWithGrantedPermissions(
			String resourceType, 
			User user,
			String permissionsType) {
		long userId = user.getId();
		log.debug("Getting resources with granted'", permissionsType, "' permissions to user '", 
				userId,"' for resource '", resourceType, "'");
		
		List<String> userPermissions = 
			getResourcesWithGrantedPermissions(
				resourceType, String.valueOf(userId), Permissions.EXTERNAL_TYPE_USER, permissionsType);
		
		log.debug("Found", userPermissions.size(), "permissions given directly to user.");
		
		Set<String> groupPermissions = 
			getResourcesWithGrantedPermissionsToRolesOfUser(
				resourceType, user ,permissionsType);
		
		log.debug("Found", groupPermissions.size(), "permissions given to roles that user has.");
		
		/* finally collect all together and sort */
		userPermissions.addAll(groupPermissions);
		List<String> resourceList = new ArrayList<String>(userPermissions);
		Collections.sort(resourceList);
		
		return resourceList;
	}
	
	public List<String> getResourcesWithGrantedPermissions(
			String resourceType, 
			String externalId,
			String externalIdType,
			String permissionsType) {
		log.debug("Getting resources with granted " + permissionsType + " permissions to externalId='" + externalId 
				+ "', externalIdType='" + externalIdType + "' for resource '" + resourceType + "'");
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("resourceType", resourceType);
		parameterMap.put("externalId", externalId);
		parameterMap.put("externalType", externalIdType);
		parameterMap.put("permissionsType", permissionsType);
		
		List<Map<String, String>> listOfMaps = queryForList(getNameSpace() + ".findResourcesWithGrantedPermissions", parameterMap);
		List<String> resourceList = new ArrayList<String>();
		
		for (Map<String, String> resultMap : listOfMaps) {
			resourceList.add(resultMap.get("resourceNamespace") + "+" + resultMap.get("resourceName"));
		}
		
		Collections.sort(resourceList);
		return resourceList;
	}
	
	/*
	 * Get permissions granted to roles to which the user belongs.
	 */
	private Set<String> getResourcesWithGrantedPermissionsToRolesOfUser(
			String resourceType, 
			User user, 
			String permissionsType) {
		Set<String> resourceSet = new HashSet<String>();
		
		for (Role role : user.getRoles()) {		
			Map<String, String> parameterMap = new HashMap<String, String>();
			parameterMap.put("resourceType", resourceType);
			parameterMap.put("externalId", String.valueOf(role.getId()));
			parameterMap.put("externalType", Permissions.EXTERNAL_TYPE_ROLE);
			parameterMap.put("permissionsType", permissionsType);
			
			List<Map<String, String>> listOfMaps = queryForList(getNameSpace() + ".findResourcesWithGrantedPermissions", parameterMap);			
			
			for (Map<String, String> resultMap : listOfMaps) {
				resourceSet.add(resultMap.get("resourceNamespace") + "+" + resultMap.get("resourceName"));
			}
		}
		
		return resourceSet;
	}
	
	public List<Permissions> getResourcePermissions(UniqueResourceName uniqueResourceName, String externalIdType) {
		log.debug("Getting " + externalIdType + " permissions to " + uniqueResourceName);		
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("resourceName", uniqueResourceName.getName());
		parameterMap.put("resourceNamespace", uniqueResourceName.getNamespace());
		parameterMap.put("resourceType", uniqueResourceName.getType());	
		parameterMap.put("externalIdType", externalIdType);
		
		List<Map<String, Object>> listOfMaps = queryForList(getNameSpace() + ".findPermissionsOfResource", parameterMap);
		
		// KEY: permissionsId, VALUE: permissions
		Map<Integer, Permissions> permissionsMap = new HashMap<Integer, Permissions>();		
		
		for (Map<String, Object> resultMap : listOfMaps) {
			int permissionsId = (Integer) resultMap.get("id");			
			Permissions p = permissionsMap.get(permissionsId);
			
			if (p == null) {
				p = new Permissions();
				p.setId(permissionsId);
				p.setUniqueResourceName(new UniqueResourceName());
				p.getUniqueResourceName().setName((String) resultMap.get("resourceName"));
				p.getUniqueResourceName().setNamespace((String) resultMap.get("resourceNamespace"));
				p.getUniqueResourceName().setType((String) resultMap.get("resourceType"));
				p.setExternalId((String) resultMap.get("externalId"));
				p.setExternalIdType((String) resultMap.get("externalIdType"));
				permissionsMap.put(p.getId(), p);
			}
			
			p.getGrantedPermissions().add((String) resultMap.get("permissionsType"));
		}
		
		List<Permissions> permissionsList = new ArrayList<Permissions>(permissionsMap.values());
		Collections.sort(permissionsList);
		return permissionsList;
	}
	
	
	public Set<String> getPublishPermissions() {
		
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("resourceType",Permissions.RESOUCE_TYPE_WMS_LAYER);
		List<Map<String, Object>> publishPermissions = queryForList(getNameSpace() + ".findPublishPermissions", parameterMap);
		
		Set<String> permissions = new HashSet<String>();
		
		//List<Map<String, String>> listOfMaps = queryForList(getNameSpace() + ".findResourcesWithGrantedPermissions", parameterMap);			
		
		for (Map<String, Object> resultMap : publishPermissions) {
			permissions.add(resultMap.get("resourceName")+":"+ resultMap.get("resourceNamespace")+":"+resultMap.get("externalId") );
		}

		return permissions;
	}
	
	
	public void deletePermissions(
			UniqueResourceName uniqueResourceName, String externalId, String externalIdType, String permissionsType) {
		Map<String, String> parameterMapPermissions = new HashMap<String, String>();
		parameterMapPermissions.put("resourceName", uniqueResourceName.getName());
		parameterMapPermissions.put("resourceNamespace", uniqueResourceName.getNamespace());
		parameterMapPermissions.put("resourceType", uniqueResourceName.getType());		
		parameterMapPermissions.put("externalId", externalId);
		parameterMapPermissions.put("externalIdType", externalIdType);
		parameterMapPermissions.put("permissionsType", permissionsType);
		
		// delete from PORTTI_PERMISSIONS table
		delete(getNameSpace() + ".deletePermissions", parameterMapPermissions);
		
		// check if there still are permissions
		Map<String, String> parameterResourceUsers = new HashMap<String, String>();
		parameterResourceUsers.put("resourceName", uniqueResourceName.getName());
		parameterResourceUsers.put("resourceNamespace", uniqueResourceName.getNamespace());
		parameterResourceUsers.put("resourceType", uniqueResourceName.getType());		
		parameterResourceUsers.put("externalId", externalId);
		parameterResourceUsers.put("externalIdType", externalIdType);
		
		List<Map<String, String>> listOfMaps = queryForList(getNameSpace() + ".findPermissionsOfResourceUser", parameterMapPermissions);
		
		// if there are no permissions, delete from PORTTI_RESOURCE_USER table
		if (listOfMaps.isEmpty()) {
			delete(getNameSpace() + ".deleteResourceUsers", parameterMapPermissions);
		}
		
		WFSLayerPermissionsStore.destroyAll();
	}

    public Map<Long, List<Permissions>> getPermissionsForLayers(List<Long> layeridList, String permissionsType) {
        Map<Long, List<Permissions>> result = new HashMap<Long, List<Permissions>>();

        Map<String, Object> parameterMapPermissions = new HashMap<String, Object>();
        parameterMapPermissions.put("permissionsType", permissionsType);
        parameterMapPermissions.put("idList", layeridList);

        List<Map<String, Object>> listOfPermissions = queryForList(getNameSpace() + ".findPermissionsForLayerIdList", parameterMapPermissions);
        List<Permissions> layerPermissions = new ArrayList<Permissions>();
        long lastLayerId = -1;
        for(Map<String, Object> rs : listOfPermissions) {
            
            final long layerId = (Integer) rs.get("layerId");
            if(lastLayerId != layerId) {
                lastLayerId = layerId;
                layerPermissions = new ArrayList<Permissions>();
                result.put(layerId, layerPermissions);
            }

            Permissions perm = new Permissions();
            perm.setExternalId((String)rs.get("externalId"));
            perm.setExternalIdType((String)rs.get("externalIdType"));
            perm.getGrantedPermissions().add((String) rs.get("permissionsType"));
            
            layerPermissions.add(perm);
        }
        return result;
    }
    
    public Map<Long, List<Permissions>> getPermissionsForBaseLayers(List<Long> layeridList, String permissionsType) {
        Map<Long, List<Permissions>> result = new HashMap<Long, List<Permissions>>();

        Map<String, Object> parameterMapPermissions = new HashMap<String, Object>();
        parameterMapPermissions.put("permissionsType", permissionsType);
        parameterMapPermissions.put("idList", layeridList);

        List<Map<String, Object>> listOfPermissions = queryForList(getNameSpace() + ".findPermissionsForBaseLayerIdList", parameterMapPermissions);
        List<Permissions> layerPermissions = new ArrayList<Permissions>();
        long lastLayerId = -1;
        for(Map<String, Object> rs : listOfPermissions) {
            
            final long layerId = (Integer) rs.get("layerId");
            if(lastLayerId != layerId) {
                lastLayerId = layerId;
                layerPermissions = new ArrayList<Permissions>();
                result.put(layerId, layerPermissions);
            }

            Permissions perm = new Permissions();
            perm.setExternalId((String)rs.get("externalId"));
            perm.setExternalIdType((String)rs.get("externalIdType"));
            perm.getGrantedPermissions().add((String) rs.get("permissionsType"));
            
            layerPermissions.add(perm);
        }
        return result;
    }
    public boolean permissionGrantedForRolesOrUser(User user, List<Permissions> permissions, String permissionsType) {
        long[] ids = new long[user.getRoles().size()];
        int index = 0;
        for(Role role : user.getRoles()) {
            ids[index] = role.getId();
            index++;
        }
        return permissionGrantedForRolesOrUser(ids, user.getId(), permissions, permissionsType);
    }
    
    public boolean permissionGrantedForRolesOrUser(long[] roleIdList, long userId, List<Permissions> permissions, String permissionsType) {
        if(permissions == null || permissions.isEmpty()) {
            return false;
        }
        for(Permissions perm : permissions) {
            final long externId = Long.parseLong(perm.getExternalId());
            if(!perm.getGrantedPermissions().contains(permissionsType)) {
                // type not found -> skip to next
                continue;
            }
            if(Permissions.EXTERNAL_TYPE_USER.equals(perm.getExternalIdType()) &&
                    userId == externId) {
                return true;
            }
            else if(Permissions.EXTERNAL_TYPE_ROLE.equals(perm.getExternalIdType()) &&
                    arrayContainsId(roleIdList, externId)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean arrayContainsId(long[] roleIdList, final long roleId) {
        for(long id : roleIdList) {
            if(id == roleId) {
                return true;
            }
        }
        return false;
    }
    
    public List<Map<String,Object>> getListOfMaplayerIdsForViewPermissionByUser(User user) {

        Map<String, Object> hm = new HashMap<String, Object>();
        hm.put("idList", getExternalIdList(user));
        
        List<Map<String,Object>> results = queryForList(getNameSpace() + ".findMaplayerIdsForViewPermissionsByExternalIds", hm);
        return results;
    }
    
    public boolean hasViewPermissionForLayerByLayerId(User user, long layerId) {
        
        Map<String, Object> hm = new HashMap<String, Object>();
        hm.put("id", layerId);
        hm.put("idList", getExternalIdList(user));
        
        return (queryForList(getNameSpace() + ".hasViewPermissionForLayerByLayerId", hm) != null);
        
    }
    
    private List<String> getExternalIdList(User user) {
        List<String> externalIds = new ArrayList<String>();
        externalIds.add(String.valueOf(user.getId()));
        for (Role role : user.getRoles()) {
            externalIds.add(String.valueOf(role.getId()));
        }
        
        return externalIds;
    }
}

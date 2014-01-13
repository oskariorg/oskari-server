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
		
		// check if row already exists in the "oskari_resource" table
		Map<String, String> parameterResource = new HashMap<String, String>();

        parameterResource.put("resourceType", uniqueResourceName.getType());
        parameterResource.put("resourceMapping", uniqueResourceName.getNamespace() +"+"+ uniqueResourceName.getName());

        Integer resourceIdInteger = queryForObject(getNameSpace() + ".findResource", parameterResource);

		if (resourceIdInteger == null) {
           insert(getNameSpace() + ".insertResource", parameterResource);
           resourceIdInteger = queryForObject(getNameSpace() + ".findResource", parameterResource);
		}

        int resourceId = resourceIdInteger.intValue();

        Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("oskariResourceId", new Integer(resourceId));
        paramMap.put("externalType", externalIdType);
		paramMap.put("permission", permissionsType);
        paramMap.put("externalId", externalId);

        Integer permissionId = queryForObject(getNameSpace() + ".findPermission", paramMap);

        if( permissionId == null) {
            insert(getNameSpace() + ".insertPermission", paramMap);
        }

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
		parameterMap.put("permission", permissionsType);
		
		List<Map<String, String>> listOfMaps = queryForList(getNameSpace() + ".findResourcesWithGrantedPermissions", parameterMap);
		List<String> resourceList = new ArrayList<String>();
		
		for (Map<String, String> resultMap : listOfMaps) {
           resourceList.add(resultMap.get("resourceMapping"));
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
			parameterMap.put("permission", permissionsType);

         	List<Map<String, String>> listOfMaps = queryForList(getNameSpace() + ".findResourcesWithGrantedPermissions", parameterMap);

				for (Map<String, String> resultMap : listOfMaps) {
                String resourceMapping =  resultMap.get("resourceMapping");
                if ("layerclass".equals(resourceType)) {
                    resourceSet.add(resourceMapping.substring(4)); // TODO: fix this
                } else {
				    resourceSet.add(resourceMapping);
                }
			}
		}
		
		return resourceSet;
	}
	
	public List<Permissions> getResourcePermissions(UniqueResourceName uniqueResourceName, String externalIdType) {
		log.debug("Getting " + externalIdType + " permissions to " + uniqueResourceName);		
		//TODO: this
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("resourceMapping", uniqueResourceName.getNamespace() +"+"+ uniqueResourceName.getName());
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
				p.getUniqueResourceName().setName((String.valueOf(resultMap.get("resourceMapping")).split("\\+")[0])); //TODO: fix this split is not good
				p.getUniqueResourceName().setNamespace((String.valueOf(resultMap.get("resourceMapping")).split("\\+")[1]));
				p.getUniqueResourceName().setType((String) resultMap.get("resourceType"));
				p.setExternalId((String) resultMap.get("externalId"));
				p.setExternalIdType((String) resultMap.get("externalType"));
				permissionsMap.put(p.getId(), p);

                // id, resource_mapping, resource_type, external_id,external_type

			}
			
			p.getGrantedPermissions().add((String) resultMap.get("permissionsType"));
		}
		
		List<Permissions> permissionsList = new ArrayList<Permissions>(permissionsMap.values());
		Collections.sort(permissionsList);
		return permissionsList;
	}
	

    public Set<String> getPublishPermissions() {

		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("resourceType",Permissions.RESOURCE_TYPE_MAP_LAYER);
		List<Map<String, Object>> publishPermissions = queryForList(getNameSpace() + ".findPublishPermissions", parameterMap);
		
		Set<String> permissions = new HashSet<String>();
		
		for (Map<String, Object> resultMap : publishPermissions) {
            permissions.add(resultMap.get("resourceMapping")+":"+resultMap.get("externalId") );
		}

		return permissions;
	}


    public Set<String> getEditPermissions() {

        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("resourceType",Permissions.RESOURCE_TYPE_MAP_LAYER);
        List<Map<String, Object>> publishPermissions = queryForList(getNameSpace() + ".findEditPermissions", parameterMap);

        Set<String> permissions = new HashSet<String>();

        for (Map<String, Object> resultMap : publishPermissions) {
            permissions.add(resultMap.get("resourceMapping")+":"+resultMap.get("externalId") );
        }

        return permissions;
    }
	
	
	public void deletePermissions(
			UniqueResourceName uniqueResourceName, String externalId, String externalIdType, String permissionsType) {

        // get resource_id by by uniqueResourceName (resource_mapping)

        Map<String, String> parameterDelete = new HashMap<String, String>();
        parameterDelete.put("resourceMapping", uniqueResourceName.getNamespace() + "+" + uniqueResourceName.getName());
        parameterDelete.put("externalId", externalId);
        parameterDelete.put("externalType", externalIdType);
        parameterDelete.put("permission",permissionsType);

        Object oskariResourceObject =  queryForObject(getNameSpace() + ".findOskariResourceId", parameterDelete);

        if (oskariResourceObject != null) {
            long oskariResourceId = ((Long)oskariResourceObject).longValue();

            delete(getNameSpace() + ".deletePermission",oskariResourceId);
        }

		WFSLayerPermissionsStore.destroyAll();
	}

    public Map<Long, List<Permissions>> getPermissionsForLayers(List<Long> layeridList, String permissionsType) {
        Map<Long, List<Permissions>> result = new HashMap<Long, List<Permissions>>();
        Map<String, Object> parameterMapPermissions = new HashMap<String, Object>();
        parameterMapPermissions.put("permissionType", permissionsType);
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
            perm.setExternalIdType((String)rs.get("externalType"));
            perm.getGrantedPermissions().add((String) rs.get("permission"));

            layerPermissions.add(perm);
        }
        return result;
    }
    // Done
    public Map<Long, List<Permissions>> getPermissionsForBaseLayers(List<Long> layeridList, String permissionsType) {
        Map<Long, List<Permissions>> result = new HashMap<Long, List<Permissions>>();

        Map<String, Object> parameterMapPermissions = new HashMap<String, Object>();
        parameterMapPermissions.put("permissionType", permissionsType);
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
            perm.setExternalIdType((String)rs.get("externalType"));
            perm.getGrantedPermissions().add((String) rs.get("permission"));
            
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
    
    public List<Map<String,Object>> getListOfMaplayerIdsForViewPermissionByUser(User user, boolean isViewPublished) {

        Map<String, Object> hm = new HashMap<String, Object>();
        hm.put("idList", getExternalIdList(user));

        List<Map<String,Object>> results = null;
        if(isViewPublished) {
            results = queryForList(getNameSpace() + ".findMaplayerIdsForViewPublishedPermissionsByExternalIds", hm);
        } else {
            results = queryForList(getNameSpace() + ".findMaplayerIdsForViewPermissionsByExternalIds", hm);
        }
        return results;
    }

    // Done
    public boolean hasViewPermissionForLayerByLayerId(User user, long layerId) {
        
        Map<String, Object> hm = new HashMap<String, Object>();
        hm.put("id", layerId);
        hm.put("idList", getExternalIdList(user));
        
        return (queryForList(getNameSpace() + ".hasViewPermissionForLayerByLayerId", hm) != null);
        
    }

    public boolean hasEditPermissionForLayerByLayerId(User user, long layerId) {

        if (user.isAdmin()) {
            return true;
        }

        Map<String, Object> hm = new HashMap<String, Object>();
        hm.put("id", layerId);
        hm.put("idList", getExternalIdList(user));

        return (queryForList(getNameSpace() + ".hasEditPermissionForLayerByLayerId", hm) != null);
    }

    public boolean hasAddLayerPermission(User user) {
        if (user.isAdmin()) {
            return true;
        }

        Map<String, Object> hm = new HashMap<String, Object>();
        hm.put("resourceMapping", "generic-functionality");
        hm.put("idList", getExternalIdList(user));

        return (queryForList(getNameSpace() + ".hasExecutePermission", hm) != null);

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

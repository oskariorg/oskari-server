package fi.mml.portti.service.db.permissions;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.domain.permissions.UniqueResourceName;
import fi.mml.portti.domain.permissions.WFSLayerPermissionsStore;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.permission.domain.Permission;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.util.*;

public class PermissionsServiceIbatisImpl extends BaseIbatisService<Permissions> implements PermissionsService {		
	
	/** Our logger */
	private static Logger log = LogFactory.getLogger(PermissionsServiceIbatisImpl.class);
    private Set<String> DYNAMIC_PERMISSIONS;

    public PermissionsServiceIbatisImpl() {
        // add any additional permissions
        DYNAMIC_PERMISSIONS = ConversionHelper.asSet(PropertyUtil.getCommaSeparatedList("permission.types"));
    }

    public Set<String> getAdditionalPermissions() {
        return DYNAMIC_PERMISSIONS;
    }

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
        }
        return permissionId;
    }

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

        // user id based permissions
        long userId = user.getId();
        log.debug("Getting resources with granted'", permissionsType, "' permissions to user '",
                userId,"' for resource '", resourceType, "'");
		List<String> userPermissions = 
			getResourcesWithGrantedPermissions(
				resourceType, String.valueOf(userId), Permissions.EXTERNAL_TYPE_USER, permissionsType);
		
		log.debug("Found", userPermissions.size(), "permissions given directly to user.");

        // user role based permissions
        final Set<String> roleIds = new HashSet<String>(user.getRoles().size());
        for(Role role : user.getRoles()) {
            roleIds.add("" + role.getId());
        }
        final Set<String> groupPermissions =
                getResourcesWithGrantedPermissions(
                        resourceType, roleIds, Permissions.EXTERNAL_TYPE_ROLE, permissionsType);
		log.debug("Found", groupPermissions.size(), "permissions given to roles that user has.");
		
		// finally collect all together and sort
		List<String> resourceList = new ArrayList<String>(userPermissions.size() + groupPermissions.size());
        resourceList.addAll(userPermissions);
        resourceList.addAll(groupPermissions);
        // sort permissions
		Collections.sort(resourceList);
		
		return resourceList;
	}
    private Set<String> getResourcesWithGrantedPermissions(
            String resourceType,
            Set<String> externalId,
            String externalIdType,
            String permissionsType) {

        // declare sorted set
        final Set<String> result = new TreeSet<String>();
        if(externalId == null || externalId.isEmpty()) {
            log.warn("Tried to get permissions without externalIds. ResourceType", resourceType, "Permission type", permissionsType, " ExternalIdType", externalIdType);
            return result;
        }

        log.debug("Getting resources with granted", permissionsType, "permissions for resourceType", resourceType,
                " with externalIdType", externalIdType, "and idList of", externalId);

        final Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("resourceType", resourceType);
        // ibatis couldn't handle a set param out of the box so wrapping it in list
        // TODO: check if there is a way to use sets with ibatis
        parameterMap.put("externalId", new ArrayList<String>(externalId));
        parameterMap.put("externalType", externalIdType);
        parameterMap.put("permission", permissionsType);

        final List<String> permittedResources = queryForList(getNameSpace() + ".findResourcesWithGrantedPermissions", parameterMap);
        result.addAll(permittedResources);
        return result;
    }
	
	public List<String> getResourcesWithGrantedPermissions(
			String resourceType, 
			String externalId,
			String externalIdType,
			String permissionsType) {

        // wrap single id to a set for convenience
        final Set<String> idList = new HashSet<String>(1);
        idList.add(externalId);

        final Set<String> sortedPermissions = getResourcesWithGrantedPermissions(resourceType, idList, externalIdType, permissionsType);
        // TODO: change signature to Set instead of List so we can get rid of this
        final List<String> results = new ArrayList<String>();
        results.addAll(sortedPermissions);
        return results;
	}

	public List<Permissions> getResourcePermissions(UniqueResourceName uniqueResourceName, String externalIdType) {
		log.debug("Getting " + externalIdType + " permissions to " + uniqueResourceName);
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
                final String[] mapping = String.valueOf(resultMap.get("resourceMapping")).split("\\+");
                p.getUniqueResourceName().setName(mapping[0]);
                p.getUniqueResourceName().setNamespace(mapping[1]);
                p.getUniqueResourceName().setType((String) resultMap.get("resourceType"));
                p.setExternalId((String) resultMap.get("externalId"));
                p.setExternalIdType((String) resultMap.get("externalIdType"));
                permissionsMap.put(p.getId(), p);
			}
			
			p.getGrantedPermissions().add((String) resultMap.get("permissions"));
		}
		
		List<Permissions> permissionsList = new ArrayList<Permissions>(permissionsMap.values());
		Collections.sort(permissionsList);
		return permissionsList;
	}
    /**
	 *   Use getPublishPermissions(String resourceType) instead.
	 */
    @Deprecated
    public Set<String> getPublishPermissions() {
        return getPublishPermissions(Permissions.RESOURCE_TYPE_MAP_LAYER);
	}

    public Set<String> getPublishPermissions(String resourceType) {

		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("resourceType",resourceType);
		List<Map<String, Object>> publishPermissions = queryForList(getNameSpace() + ".findPublishPermissions", parameterMap);

		Set<String> permissions = new HashSet<String>();

		for (Map<String, Object> resultMap : publishPermissions) {
            permissions.add(resultMap.get("resourceMapping")+":"+resultMap.get("externalId") );
		}

		return permissions;
	}

    public Set<String> getDownloadPermissions() {
        return getDownloadPermissions(Permissions.RESOURCE_TYPE_MAP_LAYER);
    }

    public Set<String> getDownloadPermissions(String resourceType) {

        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("resourceType",resourceType);
        List<Map<String, Object>> downloadPermissions = queryForList(getNameSpace() + ".findDownloadPermissions", parameterMap);

        Set<String> permissions = new HashSet<String>();

        for (Map<String, Object> resultMap : downloadPermissions) {
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
        log.debug("Finding resource permission with:", parameterDelete);

        Object oskariPermissionIdObject =  queryForObject(getNameSpace() + ".findOskariPermissionId", parameterDelete);

        if (oskariPermissionIdObject != null) {
            long oskariPermissionId = ((Long)oskariPermissionIdObject).longValue();
            log.info("Deleting permission with id:", oskariPermissionId);

            delete(getNameSpace() + ".deletePermission",oskariPermissionId);
            // flush permissions for WFS transport
            WFSLayerPermissionsStore.destroyAll();
        }

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

    /**
     * Deprecated! Base layers are no longer separated from other layers, use getPermissionsForLayers() instead.
     * As such this method doesn't work correctly!
     * @param layeridList
     * @param permissionsType
     * @return
     */
    @Deprecated
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


        final List<Object> permissions = queryForList(getNameSpace() + ".hasEditPermissionForLayerByLayerId", hm);
        log.debug("Edit permissions:", permissions);
        return (permissions != null && !permissions.isEmpty());
    }

    public boolean hasAddLayerPermission(User user) {
        if (user.isAdmin()) {
            return true;
        }

        Map<String, Object> hm = new HashMap<String, Object>();
        hm.put("resourceMapping", "generic-functionality");
        hm.put("idList", getExternalIdList(user));

        final List<Object> permissions = queryForList(getNameSpace() + ".hasExecutePermission", hm);
        log.debug("Add permissions:", permissions);
        return (permissions != null && !permissions.isEmpty());

    }

    private List<String> getExternalIdList(User user) {
        List<String> externalIds = new ArrayList<String>();
        externalIds.add(String.valueOf(user.getId()));
        for (Role role : user.getRoles()) {
            externalIds.add(String.valueOf(role.getId()));
        }
        
        return externalIds;
    }

    /**
     * Returns resource for id or null if not found
     * @param id
     * @return
     */
    private Resource getResource(final long id) {
        final Resource resource = queryForObject(getNameSpace() + ".findResourceById", id);
        log.debug("Resource:", resource);
        return resource;
    }

    public Resource getResource(final String type, final String mapping) {
        log.debug("Getting permissions for", type, " with mapping", mapping);

        final Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("type", type);
        parameterMap.put("mapping", mapping);

        Resource resource = queryForObject(getNameSpace() + ".findResourceWithMapping", parameterMap);

        if(resource == null) {
            resource = new Resource();
            resource.setMapping(mapping);
            resource.setType(type);
            log.info("Resource not found, returning empty object");
        }
        log.debug("Resource:", resource);
        return resource;
    }

    public Resource findResource(final Resource resource) {
        if(resource == null) {
            return null;
        }

        // try to find with id
        if(resource.getId() != -1) {
            // check mapping for existing by id
            return getResource(resource.getId());
        }
        // try to find with mapping
        final Resource dbRes = getResource(resource.getType(), resource.getMapping());
        if(dbRes.getId() != -1) {
            return dbRes;
        }
        return null;
    }

    public Resource saveResourcePermissions(final Resource resource) {
        if(resource == null) {
            return null;
        }
        // ensure resource is in db
        Resource res = findResource(resource);
        if(res == null) {
            res = createResourceRow(resource);
        }
        // double check we managed to insert or find
        if(res == null || res.getId() == -1) {
            log.error("Something went wrong with inserting the resource, can't find it in DB", resource);
            return null;
        }

        // set up id for resource
        resource.setId(res.getId());
        // remove all previous permissions
        removeResourcePermissions(resource);
        // persist resource.getPermissions() to DB
        for(Permission permission : resource.getPermissions()) {
            insertPermission(resource, permission, false);
        }
        // return object through db query
        return findResource(resource);
    }

    private void removeResourcePermissions(final Resource resource) {
        if(resource == null || resource.getId() == -1) {
            return;
        }
        log.debug("Deleting permissions for resource:", resource);
        delete(getNameSpace() + ".deleteResourcePermissions", resource.getId());
    }

    private Resource createResourceRow(final Resource resource) {
        if(resource == null) {
            return null;
        }

        return createResourceRow(resource.getType(), resource.getMapping());
    }
    public void deleteResource(final Resource resource) {
        removeResourcePermissions(resource);
        log.debug("Deleting permissions for resource:", resource);
        delete(getNameSpace() + ".deleteResource", resource.getId());
    }

    private Resource createResourceRow(final String type, final String mapping) {
        log.debug("Creating resource row for type:", type, "mapping:", mapping);
        Map<String, String> parameterResource = new HashMap<String, String>();
        parameterResource.put("type", type);
        parameterResource.put("mapping", mapping);

        insert(getNameSpace() + ".createResource", parameterResource);
        return getResource(type, mapping);
    }

    private boolean insertPermission(final Resource resource, final Permission permission) {
        return insertPermission(resource, permission, true);
    }
    private boolean insertPermission(final Resource resource, final Permission permission, final boolean checkExisting) {
        if(resource == null || permission == null || resource.getId() == -1) {
            return false;
        }

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("oskariResourceId", resource.getId());
        paramMap.put("externalType", permission.getExternalType());
        paramMap.put("permission", permission.getType());
        paramMap.put("externalId", permission.getExternalId());

        if(checkExisting) {
            Integer permissionId = queryForObject(getNameSpace() + ".findPermission", paramMap);
            if( permissionId != null) {
                // exists
                return true;
            }
        }

        insert(getNameSpace() + ".insertPermission", paramMap);
        return true;
    }

}

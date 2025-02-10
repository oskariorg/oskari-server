package org.oskari.permissions;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.db.DatasourceHelper;
import org.oskari.user.Role;
import org.oskari.user.User;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.mybatis.MyBatisHelper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.PermissionExternalType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Oskari
public class PermissionServiceMybatisImpl extends PermissionService {

    private static final Class<ResourceMapper> MAPPER = ResourceMapper.class;
    private static final Logger LOG = LogFactory.getLogger(PermissionServiceMybatisImpl.class);

    private final SqlSessionFactory factory;
    private final Cache<Resource> cache;

    public PermissionServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public PermissionServiceMybatisImpl(DataSource ds) {
        super();
        if (ds == null) {
            LOG.warn("DataSource was null, all future calls will throw NPEs!");
            factory = null;
        } else {
            factory = initializeMyBatis(ds);
        }
        cache = CacheManager.getCache(PermissionServiceMybatisImpl.class.getName());
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, Resource.class, Permission.class);
        MyBatisHelper.addMappers(configuration, MAPPER);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public List<Resource> findResourcesByUser(User user, ResourceType type) {
        // TODO: add userId and user.getRoles() to query and remove filtering on code
        List<Resource> all = findResourcesByType(type);
        return all.stream()
                .filter(resource -> hasUserAnyPermission(user, resource))
                .collect(Collectors.toList());
    }

    private boolean hasUserAnyPermission(User user, Resource resource) {
        return resource.getPermissions().stream()
                .anyMatch(p -> hasUserPermission(user, p));
    }

    private boolean hasUserPermission(User user, Permission permission) {
        boolean userHasRoleWithId = permission.getExternalType() == PermissionExternalType.ROLE
                && user.hasRoleWithId(permission.getExternalId());

        if (userHasRoleWithId) {
            return true;
        } else if(user.isGuest()) {
            return false;
        }

        return permission.getExternalType() == PermissionExternalType.USER
                && user.getId() == permission.getExternalId();
    }

    /**
     * For admin ui/listing permissions for resources
     * @param type
     * @return
     */
    public List<Resource> findResourcesByType(ResourceType type) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).findByType(type.name());
        }
    }

    @Override
    public Optional<Resource> findResource(int id) {
        try (SqlSession session = factory.openSession()) {
            return Optional.ofNullable(session.getMapper(MAPPER).findById(id));
        }
    }

    @Override
    public Optional<Resource> findResource(ResourceType type, String mapping) {
        return findResource(type.name(), mapping);
    }

    @Override
    public Optional<Resource> findResource(String type, String mapping) {
        String cacheKey = getCacheKey(type, mapping);
        Resource resource = cache.get(cacheKey);
        if (resource == null) {
            try (SqlSession session = factory.openSession()) {
                resource = session.getMapper(MAPPER).findByTypeAndMapping(type, mapping);
            }
            if (resource != null) {
                cache.put(cacheKey, resource);
            }
        }
        return Optional.ofNullable(resource);
    }

    @Override
    public void saveResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Tried to save null resource");
        }

        if (exists(resource)) {
            setPermissions(resource.getId(), resource.getPermissions());
            cache.remove(getCacheKey(resource));
        } else {
            insertResource(resource);
        }
    }

    private boolean exists(Resource resource) {
        String cacheKey = getCacheKey(resource);
        if (cache.get(cacheKey) != null) {
            return true;
        }
        try (SqlSession session = factory.openSession()) {
            // Prefer searching by id
            if (resource.getId() != -1) {
                return session.getMapper(MAPPER).existsById(resource.getId());
            }
            return session.getMapper(MAPPER).existsByTypeAndMapping(resource.getType(), resource.getMapping());
        }
    }

    public Set<String> getResourcesWithGrantedPermissions(String resourceType, User user, String permissionsType) {

        // user role based permissions
        final Set<Long> roleIds = user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        final Set<String> rolePermissions =
                getResourcesWithGrantedPermissions(
                        resourceType, roleIds, PermissionExternalType.ROLE, permissionsType);

        if (!user.isGuest()) {
            // user id based permissions are only valid for non-guests
            Set<String> userPermissions =
                    getResourcesWithGrantedPermissions(
                            resourceType, Collections.singleton(user.getId()), PermissionExternalType.USER, permissionsType);
            rolePermissions.addAll(userPermissions);
        }
        return rolePermissions;
    }

    private Set<String> getResourcesWithGrantedPermissions(
            String resourceType,
            Set<Long> externalId,
            PermissionExternalType externalIdType,
            String permissionsType) {

        if (externalId == null || externalId.isEmpty()) {
            return Collections.emptySet();
        }

        try (SqlSession session = factory.openSession(false)) {
            ResourceMapper mapper = session.getMapper(MAPPER);
            String commaseparatedIds = externalId.stream()
                    .map(num -> Long.toString(num))
                    .collect(Collectors.joining(","));
            return mapper.findMappingsForPermission(
                    resourceType, externalIdType, permissionsType, commaseparatedIds);
        }
    }

    @Override
    public void insertResource(Resource resource) {
        try (SqlSession session = factory.openSession(false)) {
            ResourceMapper mapper = session.getMapper(MAPPER);
            mapper.insertResource(resource);
            for (Permission permission : resource.getPermissions()) {
                mapper.insertPermission(permission, resource.getId());
            }
            session.commit();
        }
        cache.put(getCacheKey(resource), resource);
    }

    private void setPermissions(int resourceId, List<Permission> permissions) {
        try (SqlSession session = factory.openSession(false)) {
            ResourceMapper mapper = session.getMapper(MAPPER);
            mapper.deletePermissions(resourceId);
            for (Permission permission : permissions) {
                mapper.insertPermission(permission, resourceId);
            }
            session.commit();
        }
    }

    @Override
    public void deleteResource(Resource resource) {
        try (SqlSession session = factory.openSession(false)) {
            ResourceMapper mapper = session.getMapper(MAPPER);
            mapper.deletePermissions(resource.getId());
            mapper.deleteResource(resource);
            session.commit();
        }
        cache.remove(getCacheKey(resource));
    }

    private String getCacheKey(Resource resource) {
        return getCacheKey(resource.getType(), resource.getMapping());
    }

    private String getCacheKey(String type, String mapping) {
        return type + "_" + mapping;
    }

}

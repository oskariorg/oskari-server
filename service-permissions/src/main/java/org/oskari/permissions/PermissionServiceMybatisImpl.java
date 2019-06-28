package org.oskari.permissions;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.mybatis.MyBatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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

    public PermissionServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public PermissionServiceMybatisImpl(DataSource ds) {
        if (ds == null) {
            LOG.warn("DataSource was null, all future calls will throw NPEs!");
            factory = null;
        } else {
            factory = MyBatisHelper.initMyBatis(ds, MAPPER);
        }
    }


    public List<Resource> findResourcesByUser(User user) {
        try (SqlSession session = factory.openSession()) {
            List<Resource> all = session.getMapper(MAPPER).findAll();
            /*
            all.stream().filter(resource -> {
                resource.user.getRoles()
            })
            */
            return all;
        }
    }
    public List<Resource> findResourcesByUser(User user, ResourceType type) {
        try (SqlSession session = factory.openSession()) {
            List<Resource> all = session.getMapper(MAPPER).findByType(type.name());
            return all;
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
        try (SqlSession session = factory.openSession()) {
            return Optional.ofNullable(session.getMapper(MAPPER).findByTypeAndMapping(type, mapping));
        }
    }

    private Optional<Resource> findResource(final Resource resource) {
        if (resource == null) {
            return Optional.empty();
        }

        // try to find with id
        if (resource.getId() != -1) {
            // check mapping for existing by id
            return findResource(resource.getId());
        }
        // try to find with mapping
        return findResource(resource.getType(), resource.getMapping());
    }

    public void saveResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Tried to save null resource");
        }
        // ensure resource is in db
        Optional<Resource> res = findResource(resource);

        if (res.isPresent()) {
            setPermissions(res.get().getId(), resource.getPermissions());
        } else {
            insertResource(resource);
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
    }

}

package org.oskari.permissions;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.mybatis.MyBatisHelper;

public class PermissionServiceMybatisImpl extends PermissionService {

    private static final Class<ResourceMapper> MAPPER = ResourceMapper.class;

    private final SqlSessionFactory factory;

    public PermissionServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public PermissionServiceMybatisImpl(DataSource ds) {
        this.factory = MyBatisHelper.initMyBatis(ds, MAPPER);
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
        if(resource == null) {
            return null;
        }

        // try to find with id
        if(resource.getId() != -1) {
            // check mapping for existing by id
            return findResource(resource.getId());
        }
        // try to find with mapping
        return findResource(resource.getType(), resource.getMapping());
    }

    public void saveResource(Resource resource) {
        if(resource == null) {
            throw new IllegalArgumentException("Tried to save null resource");
        }
        // ensure resource is in db
        Optional<Resource> res = findResource(resource);

        if(res.isPresent()) {
            setPermissions(res.get().getId(), resource.getPermissions());
        } else {
            insertResource(resource);
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

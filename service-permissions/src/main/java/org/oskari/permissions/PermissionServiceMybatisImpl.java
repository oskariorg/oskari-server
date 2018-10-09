package org.oskari.permissions;

import java.util.Optional;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.Resource;

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
    public Optional<Resource> findResource(Resource.Type type, int mapping) {
        try (SqlSession session = factory.openSession()) {
            return Optional.ofNullable(session.getMapper(MAPPER).findByTypeAndMapping(type, mapping));
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
            mapper.deletePermissions(resource);
            mapper.deleteResource(resource);
            session.commit();
        }
    }

}

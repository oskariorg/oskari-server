package org.oskari.service.maplayer;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.mybatis.MyBatisHelper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.util.List;

@Oskari("MaplayerGroup")
public class OskariMapLayerGroupServiceMybatisImpl extends OskariMapLayerGroupService {

    private static final Class<MapLayerGroupMapper> MAPPER = MapLayerGroupMapper.class;
    private static final Logger LOG = LogFactory.getLogger(OskariMapLayerGroupServiceMybatisImpl.class);

    private final SqlSessionFactory factory;
    private final Cache<MaplayerGroup> cache;

    public OskariMapLayerGroupServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public OskariMapLayerGroupServiceMybatisImpl(DataSource ds) {
        super();
        if (ds == null) {
            LOG.warn("DataSource was null, all future calls will throw NPEs!");
            factory = null;
        } else {
            Configuration configuration = MyBatisHelper.getConfig(ds);
            configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
            MyBatisHelper.addMappers(configuration, MAPPER);
            factory = MyBatisHelper.build(configuration);

        }
        cache = CacheManager.getCache(OskariMapLayerGroupServiceMybatisImpl.class.getName());
    }

    public int insert(MaplayerGroup group) {
        try (SqlSession session = factory.openSession(false)) {
            session.getMapper(MAPPER).insert(group);
            session.commit();
        }
        cache(group);
        return group.getId();
    }
    public void update(MaplayerGroup group) {
        try (SqlSession session = factory.openSession(false)) {
            session.getMapper(MAPPER).update(group);
            session.commit();
        }
        cache(group);
    }
    public void delete(MaplayerGroup group) {
        try (SqlSession session = factory.openSession(false)) {
            session.getMapper(MAPPER).delete(group);
            session.commit();
        }
        cache.remove(getCacheKey(group));
    }

    private void cache(MaplayerGroup group) {
        cache.put(getCacheKey(group), group);
    }
    private String getCacheKey(MaplayerGroup group) {
        return getCacheKey(group.getId());
    }
    private String getCacheKey(int id) {
        return Integer.toString(id);
    }

    @Override
    public List<MaplayerGroup> findAll() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).findAll();
        }
    }

    @Override
    public MaplayerGroup find(int id) {
        MaplayerGroup group = cache.get(getCacheKey(id));
        if (group != null) {
            return group;
        }
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).findById(id);
        }
    }

    @Override
    public MaplayerGroup findByName(String name) {
        // force it as there are not many groups usually and
        // this is only intended to be used when inserting layers with flyway scripts
        List<MaplayerGroup> groups = findAll();
        return groups.stream()
                .filter(g -> g.getLocale().toString().contains(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<MaplayerGroup> findByParentId(int groupId) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).findByParentId(groupId);
        }
    }

    @Override
    public void updateOrder(MaplayerGroup group) {
        try (SqlSession session = factory.openSession(false)) {
            session.getMapper(MAPPER).updateOrder(group.getId(), group.getOrderNumber());
            session.commit();
        }
        cache.remove(getCacheKey(group));
    }

    @Override
    public void updateGroupParent(int groupId, int newParentId) {
        try (SqlSession session = factory.openSession(false)) {
            session.getMapper(MAPPER).updateGroupParent(groupId, newParentId);
            session.commit();
        }
        cache.remove(getCacheKey(groupId));
    }

    @Override
    public void flushCache() {
        cache.flush(true);
    }
}

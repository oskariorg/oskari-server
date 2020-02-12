package fi.nls.oskari.map.layer.group.link;

import java.util.List;

import javax.sql.DataSource;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.service.OskariComponent;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;

@Oskari("MaplayerGroupLinks")
public class OskariLayerGroupLinkServiceMybatisImpl extends OskariLayerGroupLinkService {

    private static final Logger LOG = LogFactory.getLogger(CapabilitiesCacheServiceMybatisImpl.class);

    private final SqlSessionFactory factory;

    public OskariLayerGroupLinkServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public OskariLayerGroupLinkServiceMybatisImpl(DataSource ds) {
        if (ds == null) {
            LOG.warn("DataSource was null, all future calls will throw NPEs!");
            factory = null;
        } else {
            factory = MyBatisHelper.initMyBatis(ds, OskariLayerGroupLinkMapper.class);
        }
    }

    private OskariLayerGroupLinkMapper getMapper(SqlSession session) {
        return session.getMapper(OskariLayerGroupLinkMapper.class);
    }

    @Override
    public List<OskariLayerGroupLink> findAll() {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findAll();
        }
    }

    public List<OskariLayerGroupLink> findByLayerId(int layerId) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findByLayerId(layerId);
        }
    }

    public List<OskariLayerGroupLink> findByGroupId(int groupId) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findByGroupId(groupId);
        }
    }

    @Override
    public void insert(OskariLayerGroupLink link) {
        if (link == null) {
            return;
        }
        try (SqlSession session = factory.openSession()) {
            getMapper(session).insert(link);
            session.commit();
        }
    }

    @Override
    public void insertAll(List<OskariLayerGroupLink> links) {
        if (links == null || links.isEmpty()) {
            return;
        }
        try (SqlSession session = factory.openSession(ExecutorType.BATCH, false)) {
            OskariLayerGroupLinkMapper mapper = getMapper(session);
            for (OskariLayerGroupLink link : links) {
                mapper.insert(link);
            }
            session.commit();
        }
    }

    @Override
    public void deleteLink(int layerId, int groupId) {
        try (SqlSession session = factory.openSession(ExecutorType.BATCH, false)) {
            getMapper(session).delete(layerId, groupId);
            session.commit();
        }
    }

    @Override
    public void deleteLinksByLayerId(int layerId) {
        try (SqlSession session = factory.openSession()) {
            getMapper(session).deleteByLayerId(layerId);
            session.commit();
        }
    }

    @Override
    public void replace(OskariLayerGroupLink old, OskariLayerGroupLink link) {
        try (SqlSession session = factory.openSession()) {
            OskariLayerGroupLinkMapper mapper = getMapper(session);
            if (old.getGroupId() != link.getGroupId()) {
                mapper.delete(old.getLayerId(), old.getGroupId());
                mapper.insert(link);
            } else {
                mapper.updateOrderNumber(link);
            }
            session.commit();
        }
    }

    @Override
    public boolean hasLinks(int groupId) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).hasLinks(groupId);
        }
    }

	@Override
	public void deleteLinksByGroupId(int groupId) {
		 try (SqlSession session = factory.openSession()) {
	            getMapper(session).deleteByGroupId(groupId);
	            session.commit();
	     }
	}
}

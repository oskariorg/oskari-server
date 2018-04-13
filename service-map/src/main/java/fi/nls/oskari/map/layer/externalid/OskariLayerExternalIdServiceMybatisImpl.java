package fi.nls.oskari.map.layer.externalid;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;

public class OskariLayerExternalIdServiceMybatisImpl implements OskariLayerExternalIdService {

    private static final Logger LOG = LogFactory.getLogger(OskariLayerExternalIdServiceMybatisImpl.class);

    private final SqlSessionFactory factory;

    public OskariLayerExternalIdServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public OskariLayerExternalIdServiceMybatisImpl(DataSource ds) {
        if (ds == null) {
            LOG.warn("DataSource was null, all future calls will throw NPEs!");
            factory = null;
        } else {
            factory = MyBatisHelper.initMyBatis(ds, OskariLayerExternalIdMapper.class);
        }
    }

    private OskariLayerExternalIdMapper getMapper(SqlSession session) {
        return session.getMapper(OskariLayerExternalIdMapper.class);
    }

    @Override
    public OskariLayerExternalId findByExternalId(String externalId) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findByExternalId(externalId);
        }
    }

    @Override
    public int insert(OskariLayerExternalId link) {
        try (SqlSession session = factory.openSession()) {
            final int n = getMapper(session).insert(link);
            session.commit();
            return n;
        }
    }

    @Override
    public int delete(int layerId) {
        try (SqlSession session = factory.openSession()) {
            final int n = getMapper(session).delete(layerId);
            session.commit();
            return n;
        }
    }

}

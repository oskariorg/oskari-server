package org.oskari.service.backendstatus;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.mybatis.MyBatisHelper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.util.List;

public class BackendStatusServiceMyBatisImpl implements BackendStatusService {

    private static final Class<BackendStatusMapper> MAPPER = BackendStatusMapper.class;

    private final SqlSessionFactory factory;

    public BackendStatusServiceMyBatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public BackendStatusServiceMyBatisImpl(DataSource ds) {
        this.factory = MyBatisHelper.initMyBatis(ds, MAPPER);
    }

    @Override
    public List<BackendStatus> findAll() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).getAll();
        }
    }

    @Override
    public List<BackendStatus> findAllWithAlert() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(MAPPER).getAllAlert();
        }
    }

    @Override
    public void insertAll(List<BackendStatus> statuses) {
        try (SqlSession session = factory.openSession(ExecutorType.BATCH, false)) {
            BackendStatusMapper mapper = session.getMapper(MAPPER);
            mapper.truncate();
            for (BackendStatus status : statuses) {
                mapper.saveStatus(status);
            }
            session.commit();
        }
    }

}

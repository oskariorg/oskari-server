package org.oskari.service.backendstatus;

import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.mybatis.MyBatisHelper;

public class BackendStatusServiceMyBatisImpl implements BackendStatusService {

    private final SqlSessionFactory factory;

    public BackendStatusServiceMyBatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource());
    }

    public BackendStatusServiceMyBatisImpl(DataSource ds) {
        this.factory = MyBatisHelper.initMyBatis(ds, BackendStatusMapper.class);
    }

    @Override
    public List<BackendStatus> findAll() {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).getAll();
        }
    }

    @Override
    public List<BackendStatus> findAllWithAlert() {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).getAllAlert();
        }
    }

    @Override
    public void insertAll(List<BackendStatus> statuses) {
        try (SqlSession session = factory.openSession(ExecutorType.BATCH, false)) {
            BackendStatusMapper mapper = getMapper(session);
            mapper.truncate();
            for (BackendStatus status : statuses) {
                mapper.saveStatus(status);
            }
            session.commit();
        }
    }

    private static BackendStatusMapper getMapper(final SqlSession session) {
        return session.getMapper(BackendStatusMapper.class);
    }

}

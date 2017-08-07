package org.oskari.spatineo.monitor.backendstatus;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Data access to the Oskari Database BackendStatus information.
 */
public class BackendStatusDao {

    private final SqlSessionFactory factory;

    public BackendStatusDao(final SqlSessionFactory factory) {
        this.factory = factory;
    }

    public void insertStatuses(final List<BackendStatus> statuses) {
        try (SqlSession session = factory.openSession(false)) {
            // Truncate and insert all the records in one session == transaction
            BackendStatusMapper mapper = session.getMapper(BackendStatusMapper.class);
            mapper.truncateStatusTable();
            for (BackendStatus status : statuses) {
                mapper.saveStatus(status);
            }
            session.commit();
        }
    }

}

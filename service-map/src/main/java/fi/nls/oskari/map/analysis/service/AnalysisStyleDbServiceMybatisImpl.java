package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import javax.sql.DataSource;

public class AnalysisStyleDbServiceMybatisImpl implements AnalysisStyleDbService {

    private static final Logger LOG = LogFactory.getLogger(AnalysisStyleDbServiceMybatisImpl.class);
    private static final Class<AnalysisStyleMapper> MAPPER_CLASS = AnalysisStyleMapper.class;

    private SqlSessionFactory factory = null;

    public AnalysisStyleDbServiceMybatisImpl() {
        // Try to load analysis datasource
        DatasourceHelper helper = DatasourceHelper.getInstance();
        String prefix = AnalysisDbServiceMybatisImpl.DATASOURCE_ANALYSIS;
        String dataSourceName = helper.getOskariDataSourceName(prefix);
        DataSource dataSource = helper.getDataSource(dataSourceName);
        if (dataSource == null) {
            // If we can't find analysis datasource use default datasource
            dataSource = helper.getDataSource();
        }
        if (dataSource == null) {
            LOG.error("Failed to start, could not get datasource");
        } else {
            factory = MyBatisHelper.initMyBatis(dataSource, MAPPER_CLASS);
        }
    }

    /**
     * Insert analysis style to the database
     * @return id of the row inserted
     * @throws ServiceException if service is not initialized properly
     *         or if something goes wrong inserting the analysis style
     */
    public long insertAnalysisStyleRow(AnalysisStyle analysisStyle)
            throws ServiceException {
        if (factory == null) {
            throw new ServiceException("Service not initialized");
        }
        try (SqlSession session = factory.openSession()) {
            session.getMapper(MAPPER_CLASS).insertAnalysisStyleRow(analysisStyle);
            session.commit();
            LOG.debug("Inserted analysis style - id", analysisStyle.getId());
            return analysisStyle.getId();
        } catch (Exception e) {
            LOG.warn(e, "Failed to insert analysis style", analysisStyle);
            throw new ServiceException("Failed to insert analysis style", e);
        }
    }

}

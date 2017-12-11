package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceException;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

public class AnalysisStyleDbServiceMybatisImpl implements AnalysisStyleDbService {

    private static final Logger LOG = LogFactory.getLogger(AnalysisStyleDbServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;

    public AnalysisStyleDbServiceMybatisImpl() {
        // Try to load analysis datasource
        DatasourceHelper helper = DatasourceHelper.getInstance();
        String prefix = AnalysisDbServiceMybatisImpl.DATASOURCE_ANALYSIS;
        String dataSourceName = helper.getOskariDataSourceName(prefix);
        DataSource dataSource = helper.getDataSource(dataSourceName);
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            LOG.error("Failed to start, could not get datasource");
        } else {
            factory = initializeMyBatis(dataSource);
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(AnalysisStyle.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(AnalysisStyleMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
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
            session.getMapper(AnalysisStyleMapper.class).insertAnalysisStyleRow(analysisStyle);
            session.commit();
            LOG.debug("Inserted analysis style - id", analysisStyle.getId());
            return analysisStyle.getId();
        } catch (Exception e) {
            LOG.warn(e, "Failed to insert analysis style", analysisStyle);
            throw new ServiceException("Failed to insert analysis style", e);
        }
    }

}

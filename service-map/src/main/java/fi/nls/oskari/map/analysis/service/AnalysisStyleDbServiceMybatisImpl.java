package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

public class AnalysisStyleDbServiceMybatisImpl implements AnalysisStyleDbService {

    private static final Logger log = LogFactory.getLogger(AnalysisStyleDbServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;

    public AnalysisStyleDbServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("analysisStyle"));
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            log.error("Couldn't get datasource for analysistyledbservice");
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
     * insert Analysis_style table row
     *
     * @param analysisStyle
     */

    public long insertAnalysisStyleRow(final AnalysisStyle analysisStyle) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Insert analysisiStyle row:", analysisStyle);
            final AnalysisStyleMapper mapper = session.getMapper(AnalysisStyleMapper.class);
            mapper.insertAnalysisStyleRow(analysisStyle);
            session.commit();
            log.debug("Got analyseStyle id:", analysisStyle.getId());
        } catch (Exception e) {
            log.warn(e, "Exception when trying to add analysisStyle: ", analysisStyle);
        } finally {
            session.close();
        }
        return analysisStyle.getId();
    }
	   
	 

      


}

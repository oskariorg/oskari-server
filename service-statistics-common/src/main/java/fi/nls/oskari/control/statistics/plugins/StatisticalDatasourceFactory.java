package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayerMapper;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.service.OskariComponent;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;

/**
 * Used to create plugin instances for source
 */
public abstract class StatisticalDatasourceFactory extends OskariComponent {
    public abstract StatisticalDatasourcePlugin create(StatisticalDatasource source);

    public void setupSourceLayers(StatisticalDatasource source) {
        // Fetching the layer mapping from the database.
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        final SqlSessionFactory factory = initializeIBatis(dataSource);
        final SqlSession session = factory.openSession();
        try {
            final List<DatasourceLayer> layerRows = session.getMapper(DatasourceLayerMapper.class).getLayersForDatasource(source.getId());
            source.setLayers(layerRows);
        } finally {
            session.close();
        }
    }

    private SqlSessionFactory initializeIBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(DatasourceLayer.class);
        configuration.setLazyLoadingEnabled(true);
        // typehandlers aren't found from classpath even when annotated.
        // also important to register them before adding mappers
        configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
        configuration.addMapper(DatasourceLayerMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }
}

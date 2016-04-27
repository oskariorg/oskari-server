package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.control.statistics.db.RegionSetMapper;
import fi.nls.oskari.db.DatasourceHelper;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SMAKINEN on 27.4.2016.
 */
@Oskari
public class RegionSetServiceMybatisImpl extends RegionSetService {
    private static final Logger LOG = LogFactory.getLogger(RegionSetServiceMybatisImpl.class);
    private SqlSessionFactory factory = null;

    public RegionSetServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            LOG.error("Couldn't get datasource for statistical regionsets service");
        }
    }
    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(RegionSet.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(RegionSetMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public List<RegionSet> getRegionSets() {
        List regions = new ArrayList();

        try (SqlSession session = factory.openSession()) {
            List<RegionSet> layerMetadataRows = session.getMapper(RegionSetMapper.class).getRegionSets();
            for (RegionSet row : layerMetadataRows) {
                regions.add(row);
            }
            LOG.debug("Oskari stat regionset layers: ", regions);
        }
        return regions;
    }
    public RegionSet getRegionSet(long id) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(RegionSetMapper.class).getRegionSet(id);
        }
    }
}

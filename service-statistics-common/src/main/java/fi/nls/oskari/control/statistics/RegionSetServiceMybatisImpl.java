package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.control.statistics.db.RegionSetMapper;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.util.List;

@Oskari
public class RegionSetServiceMybatisImpl extends RegionSetService {

    private static final Logger LOG = LogFactory.getLogger(RegionSetServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;

    public RegionSetServiceMybatisImpl() {
        final DataSource dataSource = DatasourceHelper.getInstance().getDataSource();
        if (dataSource != null) {
            factory = initializeMyBatis(dataSource);
        } else {
            LOG.error("Couldn't get datasource for statistical regionsets service");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        Configuration configuration = MyBatisHelper.getConfig(dataSource, RegionSetMapper.class);
        MyBatisHelper.addAliases(configuration, RegionSet.class);
        return MyBatisHelper.build(configuration);
    }

    public List<RegionSet> getRegionSets() {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(RegionSetMapper.class).getRegionSets();
        }
    }
    public RegionSet getRegionSet(long id) {
        try (SqlSession session = factory.openSession()) {
            return session.getMapper(RegionSetMapper.class).getRegionSet(id);
        }
    }
}

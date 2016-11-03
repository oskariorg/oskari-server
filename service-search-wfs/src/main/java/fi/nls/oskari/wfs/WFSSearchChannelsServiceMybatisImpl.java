package fi.nls.oskari.wfs;

import java.util.List;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONArrayMybatisTypeHandler;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

@Oskari
public class WFSSearchChannelsServiceMybatisImpl extends WFSSearchChannelsService {
	private final static Logger LOG = LogFactory.getLogger(WFSSearchChannelsServiceMybatisImpl.class);

    private final SqlSessionFactory factory;

    public WFSSearchChannelsServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            throw new RuntimeException("Error initializing wfs search channels");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        configuration.getTypeAliasRegistry().registerAlias(WFSSearchChannelsConfiguration.class);
        configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(JSONArrayMybatisTypeHandler.class);

        configuration.addMapper(WFSChannelConfigMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public List<WFSSearchChannelsConfiguration> findChannels() {
        try (final SqlSession session = factory.openSession()) {
            WFSChannelConfigMapper mapper = session.getMapper(WFSChannelConfigMapper.class);
            List<WFSSearchChannelsConfiguration> channels = mapper.findAll();
            return channels;
        }
    }
    
    public WFSSearchChannelsConfiguration findChannelById(final long channelId) {
        try (final SqlSession session = factory.openSession()) {
            WFSChannelConfigMapper mapper = session.getMapper(WFSChannelConfigMapper.class);
            return mapper.findChannelById(channelId);
        }
    }

    public void delete(final long channelId) {
        try (final SqlSession session = factory.openSession()) {
            WFSChannelConfigMapper mapper = session.getMapper(WFSChannelConfigMapper.class);
            mapper.delete(channelId);
            session.commit();
        }
    }
    
    public synchronized long insert(final WFSSearchChannelsConfiguration channel) {
        try (final SqlSession session = factory.openSession()) {
            WFSChannelConfigMapper mapper = session.getMapper(WFSChannelConfigMapper.class);
            mapper.insert(channel);
            session.commit();
            return channel.getId();
        }
    }
    
    public void update(final WFSSearchChannelsConfiguration channel) {
        try (final SqlSession session = factory.openSession()) {
            WFSChannelConfigMapper mapper = session.getMapper(WFSChannelConfigMapper.class);
            mapper.update(channel);
            session.commit();
        }
    };
}

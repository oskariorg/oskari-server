package fi.nls.oskari.wfs;

import java.util.List;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.search.channel.WFSChannelProvider;
import fi.nls.oskari.service.OskariComponentManager;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

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
            LOG.error("Error initializing wfs search channels. Couldn't get datasource.");
            factory = null;
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, WFSSearchChannelsConfiguration.class);
        MyBatisHelper.addMappers(configuration, WFSChannelConfigMapper.class);

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
            WFSSearchChannelsConfiguration config = findChannelById(channelId);
            mapper.delete(channelId);
            session.commit();
            OskariComponentManager.getComponentOfType(WFSChannelProvider.class).channelRemoved(config);
        }
    }
    
    public synchronized long insert(final WFSSearchChannelsConfiguration channel) {
        try (final SqlSession session = factory.openSession()) {
            WFSChannelConfigMapper mapper = session.getMapper(WFSChannelConfigMapper.class);
            mapper.insert(channel);
            session.commit();
            OskariComponentManager.getComponentOfType(WFSChannelProvider.class).channelAdded(findChannelById(channel.getId()));
            return channel.getId();
        }
    }
    
    public void update(final WFSSearchChannelsConfiguration channel) {
        try (final SqlSession session = factory.openSession()) {
            WFSChannelConfigMapper mapper = session.getMapper(WFSChannelConfigMapper.class);
            mapper.update(channel);
            session.commit();
            OskariComponentManager.getComponentOfType(WFSChannelProvider.class).channelUpdated(findChannelById(channel.getId()));
        }
    };
}

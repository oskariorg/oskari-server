package fi.nls.oskari.service.capabilities;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.OskariLayer;
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

@Oskari
public class CapabilitiesCacheServiceMybatisImpl extends CapabilitiesCacheService {

    private static final Logger LOG = LogFactory.getLogger(CapabilitiesCacheServiceMybatisImpl.class);
    private SqlSessionFactory factory = null;

    public CapabilitiesCacheServiceMybatisImpl() {

        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            LOG.error("Couldn't get datasource for", getClass().getName());
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(OskariLayerCapabilities.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(CapabilitiesMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * Tries to load capabilities from the database
     * @return null if not saved to db
     */
    public OskariLayerCapabilities find(final OskariLayer layer) {
        return find(layer.getSimplifiedUrl(true), layer.getType());
    }
    /**
     * Tries to load capabilities from the database
     * @return null if not saved to db
     */
    public OskariLayerCapabilities find(final String url, final String layertype) {
        if(url == null ||layertype == null) {
            LOG.debug("Incomplete params:", url, layertype);
            return null;
        }

        LOG.debug("Load caps with params:", url, layertype);

        final SqlSession session = factory.openSession();
        try {
            final CapabilitiesMapper mapper = session.getMapper(CapabilitiesMapper.class);
            OskariLayerCapabilities capabilities = mapper.find(url.toLowerCase(), layertype.toLowerCase());
            return capabilities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load capabilities", e);
        } finally {
            session.close();
        }
    }

    /**
     * Inserts or updates a capabilities XML in database
     */
    public OskariLayerCapabilities save(final OskariLayerCapabilities capabilities) {

        final SqlSession session = factory.openSession();
        try {
            final CapabilitiesMapper mapper = session.getMapper(CapabilitiesMapper.class);
            OskariLayerCapabilities db = mapper.find(capabilities.getUrl(), capabilities.getLayertype());
            if(db != null) {
                capabilities.setId(db.getId());
                mapper.updateData(capabilities);
            }
            else {
                long id = mapper.insert(capabilities);
                capabilities.setId(id);
            }
            session.commit();
            LOG.debug("Saved cap with id", capabilities.getId());
            return capabilities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save capabilities", e);
        } finally {
            session.close();
        }
    }
}

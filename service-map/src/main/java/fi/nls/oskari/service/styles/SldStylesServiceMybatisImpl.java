package fi.nls.oskari.service.styles;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesMapper;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;

@Oskari
public class SldStylesServiceMybatisImpl extends SldStylesService {

    private static final Logger LOG = LogFactory.getLogger(SldStylesServiceMybatisImpl.class);
    private SqlSessionFactory factory = null;

    public SldStylesServiceMybatisImpl() {
    }

    private SqlSessionFactory getFactory() {
        if(factory != null) {
            return factory;
        }
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName());
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        } else {
            LOG.error("Couldn't get datasource for", getClass().getName());
        }
        return factory;
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(SldStyle.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(SldStylesMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * Tries to load sld styles from the database
     * @return null if no styles in db
     */
    public List<SldStyle> selectAll() {


        final SqlSession session = getFactory().openSession();
        try {
            final SldStylesMapper mapper = session.getMapper(SldStylesMapper.class);
            return mapper.selectAll();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load sld styles", e);
        } finally {
            session.close();
        }
    }

    public int saveStyle(SldStyle style) {


        final SqlSession session = getFactory().openSession();
        try {
            final SldStylesMapper mapper = session.getMapper(SldStylesMapper.class);

            mapper.saveStyle(style);

            session.commit();
            LOG.debug("Saved new style with id", style.getId());

            return style.getId();

        } catch (Exception e) {
            throw new RuntimeException("Failed to insert sld style", e);
        } finally {
            session.close();
        }
    }

}

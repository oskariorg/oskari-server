package fi.nls.oskari.map.view;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.view.Bundle;
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
public class BundleServiceMybatisImpl extends BundleService {

    private static final Logger log = LogFactory.getLogger(BundleServiceMybatisImpl.class);
    private Cache<Bundle> bundleCache = CacheManager.getCache(getClass().getName());

    private SqlSessionFactory factory = null;

    public BundleServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            log.error("Couldn't get datasource for bundle service");
        }
        factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(Bundle.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(BundleMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public Bundle getBundleTemplateByName(final String name) {
        log.debug("Finding bundle template by name:", name);
        Bundle bundle = bundleCache.get(name);
        if(bundle != null) {
            // return a clone so the template remains immutable from outside
            return bundle.clone();
        }
        final SqlSession session = factory.openSession();
        try {
            log.debug("Getting bundle by name: " + name);
            final BundleMapper mapper = session.getMapper(BundleMapper.class);
            return mapper.getBundleTemplateByName(name);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to get bundle with name: " + name);
        } finally {
            session.close();
        }
        return null;
    }

    public long addBundleTemplate(final Bundle bundle) {
        log.debug("Adding bundle:", bundle);
        final SqlSession session = factory.openSession();
        try {
            final BundleMapper mapper = session.getMapper(BundleMapper.class);
            long id = mapper.addBundleTemplate(bundle);
            bundle.setBundleId(id);
            log.debug("Got bundle id:", id);
            return id;
        } catch (Exception e) {
            log.warn("Unable to add bundle");
        } finally {
            session.close();
        }
        return 0;
    }

    /**
     * Preloads and caches the bundle template by bundleid(name)
     * @param bundleid
     */
    public void forceBundleTemplateCached(final String bundleid) {
        final Bundle bundle = getBundleTemplateByName(bundleid);
        if(bundle != null) {
            bundleCache.put(bundleid, bundle);
        }
    }
}
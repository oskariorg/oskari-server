package fi.nls.oskari.map.publish.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.publish.domain.TermsOfUse;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import javax.sql.DataSource;
import java.sql.Timestamp;

@Oskari
public class PublishTermsOfUseServiceMybatisImpl extends PublishTermsOfUseService {

    private static final Logger log = LogFactory.getLogger(PublishTermsOfUseServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;

    public PublishTermsOfUseServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            log.error("Couldn't get datasource for published terms of use service");
        }
        factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(PublishTermsOfUseService.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(PublishTermsOfUseMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public int insert(final TermsOfUse termsOfUse) {
        log.debug("Insert terms of use");
        final SqlSession session = factory.openSession();
        try {
            final PublishTermsOfUseMapper mapper = session.getMapper(PublishTermsOfUseMapper.class);
            mapper.insertTermsOfUse(termsOfUse);
            session.commit();
        } catch (Exception e) {
            log.warn("Unable to insert terms of use");
        } finally {
            session.close();
        }
        return -1;
    }

    public int setUserAgreed(final long userId) {
        log.debug("Set user agreed to terms of use for user id: " + userId);
        try {
            final TermsOfUse accepted = findByUserId(userId);
            if (accepted == null) {
                final TermsOfUse tou = new TermsOfUse();
                tou.setAgreed(true);
                tou.setUserid(userId);
                tou.setTime(new Timestamp(System.currentTimeMillis()));
                return insert(tou);
            }
        } catch (Exception e) {
            log.warn("Unable to set agreed to terms of use for user id: " + userId);
        }
        return -1;
    }

    public TermsOfUse findByUserId(final long userId) {
        log.debug("Find terms of use for user id: " + userId);
        final SqlSession session = factory.openSession();
        try {
            final PublishTermsOfUseMapper mapper = session.getMapper(PublishTermsOfUseMapper.class);
            return mapper.findByUserId(userId);
        } catch (Exception e) {
            log.warn("Unable to find agreed terms of use for user id: " + userId);
        } finally {
            session.close();
        }
        return null;
    }

}
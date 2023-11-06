package fi.nls.oskari.map.publish.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerMapper;
import fi.nls.oskari.map.publish.domain.TermsOfUse;
import fi.nls.oskari.mybatis.MyBatisHelper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import javax.sql.DataSource;
import java.sql.Timestamp;

@Oskari
public class PublishTermsOfUseServiceMybatisImpl extends PublishTermsOfUseService {

    private static final Logger log = LogFactory.getLogger(PublishTermsOfUseServiceMybatisImpl.class);

    private SqlSessionFactory factory;

    public PublishTermsOfUseServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            log.error("Couldn't get datasource for publish terms of use service");
        }
        factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource, PublishTermsOfUseMapper.class);
        //configuration.getTypeAliasRegistry().registerAlias(PublishTermsOfUseService.class);
        //MyBatisHelper.addAliases(configuration, PublishTermsOfUseService.class);
        //MyBatisHelper.addMappers(configuration, PublishTermsOfUseMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public boolean insert(final TermsOfUse termsOfUse) {
        log.debug("Insert terms of use");
        try (final SqlSession session = factory.openSession()) {
            final PublishTermsOfUseMapper mapper = session.getMapper(PublishTermsOfUseMapper.class);
            mapper.insertTermsOfUse(termsOfUse);
            session.commit();
            return true;
        } catch (Exception e) {
            log.warn("Unable to insert terms of use:", e.getMessage());
        }
        return false;
    }

    public boolean setUserAgreed(final long userId) {
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
        return false;
    }

    public TermsOfUse findByUserId(final long userId) {
        log.debug("Find terms of use for user id: " + userId);

        try (final SqlSession session = factory.openSession()) {
            final PublishTermsOfUseMapper mapper = session.getMapper(PublishTermsOfUseMapper.class);
            return mapper.findByUserId(userId);
        } catch (Exception e) {
            log.warn("Unable to find agreed terms of use for user id: " + userId);
        }
        return null;
    }

}
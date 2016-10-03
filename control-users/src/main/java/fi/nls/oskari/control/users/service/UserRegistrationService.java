package fi.nls.oskari.control.users.service;

import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

public class UserRegistrationService extends OskariComponent {

    private static final Logger LOG = LogFactory.getLogger(UserRegistrationService.class);

    private SqlSessionFactory factory = null;

    public UserRegistrationService() {

        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("myplaces"));
        if (dataSource != null) {
            factory = initializeMyBatis(dataSource);
        } else {
            LOG.error("Couldn't get datasource for myplaces");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(Email.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(EmailMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public Long addEmail(Email email) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            mapper.addEmail(email);
            session.commit();
            return email.getId();
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to add email:", email);
        }
        return -1l;
    }

    public Email findByToken(String uuid) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            return mapper.findByToken(uuid);
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to find by token:", uuid);
        }
        throw new RuntimeException("Couldn't get token for " + uuid);
    }

    public String findUsernameForEmail(String email) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            return mapper.findUsernameForEmail(email);
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to find by email:", email);
        }
        throw new RuntimeException("Couldn't get token for " + email);
    }

    public String findUsernameForLogin(String username) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            return mapper.findUsernameForLogin(username);
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to find by email:", username);
        }
        throw new RuntimeException("Couldn't get username for " + username);
    }

    public void deleteEmailToken(String uuid) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            mapper.deleteEmailToken(uuid);
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't delete email token for " + uuid, e);
        }
    }

    public String findEmailForUsername(String username) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            return mapper.findEmailForUsername(username);
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to find by username:", username);
        }
        throw new RuntimeException("Couldn't find email token for " + username);
    }

    public Integer findUserRoleId(String name) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            return mapper.findUserRoleId(name);
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to delete by email:", name);
        }
        throw new RuntimeException("Couldn't delete email token for " + name);
    }
}

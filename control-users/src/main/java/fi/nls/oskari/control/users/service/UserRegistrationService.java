package fi.nls.oskari.control.users.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.users.RegistrationUtil;
import fi.nls.oskari.control.users.model.EmailToken;
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
import java.util.UUID;

@Oskari
public class UserRegistrationService extends OskariComponent {

    private static final Logger LOG = LogFactory.getLogger(UserRegistrationService.class);

    private SqlSessionFactory factory = null;

    public UserRegistrationService() {

        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource();
        if (dataSource != null) {
            factory = initializeMyBatis(dataSource);
        } else {
            LOG.error("Couldn't get datasource for user registration");
        }
    }

    public EmailToken setupToken(String email) {
        EmailToken token = findTokenByEmail(email);
        if(token != null) {
            // refresh token expiry if one exists
            token.setUuid( UUID.randomUUID().toString());
            token.setExpiryTimestamp(RegistrationUtil.createExpiryTime());
            updateToken(token);
        } else {
            // create a new token
            token = new EmailToken();
            token.setEmail(email);
            token.setScreenname("");
            token.setUuid( UUID.randomUUID().toString());
            token.setExpiryTimestamp(RegistrationUtil.createExpiryTime());
            addToken(token);
        }
        return token;
    }


    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(EmailToken.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(EmailMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public Long addToken(EmailToken emailToken) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            mapper.addEmail(emailToken);
            session.commit();
            return emailToken.getId();
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to add email:", emailToken);
        }
        return -1l;
    }
    public Long updateToken(EmailToken emailToken) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            mapper.updateEmail(emailToken);
            session.commit();
            return emailToken.getId();
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to add email:", emailToken);
        }
        return -1l;
    }

    public EmailToken findTokenByEmail(String email) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            return mapper.findTokenByEmail(email.toLowerCase());
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to find token by email:", email);
        }
        throw new RuntimeException("Couldn't get token for " + email);
    }

    public EmailToken findByToken(String uuid) {
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
            return mapper.findUsernameForEmail(email.toLowerCase());
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

    public void removeTokenByUUID(String uuid) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            mapper.deleteEmailToken(uuid);
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't delete email token for " + uuid, e);
        }
    }

    public boolean isUsernameReserved(String username) {
        try (SqlSession session = factory.openSession()) {
            final EmailMapper mapper = session.getMapper(EmailMapper.class);
            Long id = mapper.isUsernameReserved(username.toLowerCase());
            return id != null && id > -1;
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to find by username:", username);
            return true;
        }
    }

}

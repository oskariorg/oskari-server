package fi.nls.oskari.user;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MybatisUserService {
    private MybatisRoleService roleService = new MybatisRoleService();

    private static final Logger log = LogFactory.getLogger(MybatisUserService.class);

    private SqlSessionFactory factory = null;

    public MybatisUserService() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            log.error("Couldn't get datasource for userservice");
        }
        factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(User.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(UsersMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public List<User> findAll(){
        final SqlSession session = factory.openSession();
        List<User> userList = null;
        try {
            log.debug("Find all users");
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            userList = mapper.findAll();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to find all users");
        } finally {
            session.close();
        }
        log.debug("Finished finding all users");
        return userList;
    }

    public Long addUser(User user) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Adding user: ", user);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            mapper.addUser(user);
            session.commit();
            log.info("Added user id: ", user.getId());
        } catch (Exception e) {
            log.warn(e, "Exception when trying to add user: ", user);
        } finally {
            session.close();
        }
        return user.getId();
    }

    public void updateUser(User user) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Updating user: ", user);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            mapper.updateUser(user);
            session.commit();
            log.info("Updated user id: " + user.getId());
        } catch (Exception e) {
            log.warn(e, "Exception when trying to update user: ", user);
        } finally {
            session.close();
        }
    }

    public User find(long id) {
        final SqlSession session = factory.openSession();
        User user = null;
        try {
            log.debug("Finding user by id: ", id);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            user = mapper.find(id);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to find user: ", user);
        } finally {
            session.close();
        }
        log.debug("Found user: " + user);
        return user;
    }

    /**
     * Returns null if doesn't match any user or username for the user that was found
     *
     * @param username
     * @param password
     * @return
     */
    public String login(final String username, final String password) {
        final SqlSession session = factory.openSession();
        String login = null;
        try {
            log.debug("Login by username and password: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            Map<String, String> params = new HashMap<String, String>(2);
            params.put("username", username);
            params.put("password", password);
            login = mapper.login(params);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to login with username: ", username);
        } finally {
            session.close();
        }
        log.info(login != null ? "Username " + login + " logged in." : "User not found with username: " + username);
        return login;
    }

    public String getPassword(final String username) {
        final SqlSession session = factory.openSession();
        String password = null;
        try {
            log.debug("Finding password by username: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            password = mapper.getPassword(username);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to get password for username: ", username);
        } finally {
            session.close();
        }
        log.debug("Found password");
        return password;
    }

    public User findByUserName(String username) {
        final SqlSession session = factory.openSession();
        User user = null;
        try {
            log.debug("Finding user by username: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            user = mapper.findByUserName(username);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to find user by email: ", username);
        } finally {
            session.close();
        }
        log.debug("Found user: " + user);
        loadRoles(user);
        return user;
    }

    public User findByEmail(String email) {
        final SqlSession session = factory.openSession();
        User user = null;
        try {
            log.debug("Finding user by email: ", email);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            user = mapper.findByEmail(email.toLowerCase());
        } catch (Exception e) {
            log.warn(e, "Exception when trying to find user by email: ", email);
        } finally {
            session.close();
        }
        log.debug("Found user: " + user);
        loadRoles(user);
        return user;
    }

    private void loadRoles(User user) {
        if (user == null) {
            return;
        }
        List<Role> roleList = roleService.findByUserId(user.getId());
        for (Role role : roleList) {
            user.addRole(role.getId(), role.getName());
        }
    }

    public void delete(long id) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Deleting user by id: ", id);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            mapper.delete(id);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to delete user by id: ", id);
        } finally {
            session.close();
        }
        log.info("Deleted user with id: " + id);
    }

    public void setPassword(String username, String password) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Setting password to user: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            Map<String, String> params = new HashMap<String, String>(2);
            params.put("username", username);
            params.put("password", password);
            mapper.setPassword(params);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to set password to username: ", username);
        } finally {
            session.close();
        }
        log.info("Set password for username: " + username);
    }

    public void updatePassword(String username, String password) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Updating password to user: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            Map<String, String> params = new HashMap<String, String>(2);
            params.put("username", username);
            params.put("password", password);
            mapper.updatePassword(params);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to update password to username: ", username);
        } finally {
            session.close();
        }
        log.info("Updated password for username: " + username);
    }

    public void deletePassword(String username) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Deleting password for username: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            mapper.deletePassword(username);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to delete password for username: ", username);
        } finally {
            session.close();
        }
        log.info("Deleted password for username: " + username);
    }

}

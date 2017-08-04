package fi.nls.oskari.user;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.ontology.service.KeywordMapper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MybatisRoleService {

    private static final Logger log = LogFactory.getLogger(MybatisRoleService.class);

    private SqlSessionFactory factory = null;

    public MybatisRoleService() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("roles"));
        if (dataSource != null) {
            factory = initializeMyBatis(dataSource);
        } else {
            log.error("Couldn't get datasource for  rolesservice");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(KeywordMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public List<Role> findAll(){
        //TODO implement
        return Collections.emptyList();
    }

    public void deleteUsersRoles(long userId){
        //TODO
    }

    public void delete(long userId){
        //TODO
    }

    public long insert(Role role){
        //TODO
        return 0;
    }

    public List<Role> findByUserName(String username) {
        if(username == null) {
            return Collections.emptyList();
        }

        final SqlSession session = factory.openSession();
        List<Role> roleList = null;
        try {
            log.debug("Finding roles by username: ", username);

            final RolesMapper mapper = session.getMapper(RolesMapper.class);
            roleList =  mapper.findByUserName(username);
            if(roleList == null) {
                roleList = Collections.emptyList();
            }
            log.debug("Found roles: ", roleList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to load roles with username: ", username);
        } finally {
            session.close();
        }
        return roleList;
    }
    public List<Role> findByUserId(long userId) {
        final SqlSession session = factory.openSession();
        List<Role> roleList = null;
        try {
            log.debug("Finding roles by user id: ", userId);

            final RolesMapper mapper = session.getMapper(RolesMapper.class);
            roleList =  mapper.findByUserId(userId);
            if(roleList == null) {
                roleList = Collections.emptyList();
            }
            log.debug("Found roles: ", roleList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to load roles with user id: ", userId);
        } finally {
            session.close();
        }
        return roleList;
    }

    public Role findGuestRole() {
        final SqlSession session = factory.openSession();
        List<Role> roleList = null;
        try {
            log.debug("Finding guest role by user id: ");

            final RolesMapper mapper = session.getMapper(RolesMapper.class);
            roleList =  mapper.findGuestRoles();
            log.debug("Found roles: ", roleList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to find quest roles");
        } finally {
            session.close();
        }
        if(roleList.isEmpty()) {
            return null;
        }
        return roleList.get(0);
    }

    /**
     * Same as linkRoleToUser except skips check if user has role already
     * @param roleId
     * @param userId
     */
    public void linkRoleToNewUser(long roleId, long userId) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Linking role to user id : ", userId);
            final RolesMapper mapper = session.getMapper(RolesMapper.class);
            final Map<String, Long> params = new HashMap<String, Long>();
            params.put("role_id", roleId);
            params.put("user_id", userId);
            mapper.linkRoleToNewUser(params);
            log.debug("Linked role to new user with id: ", userId);
        } catch (Exception e) {
            log.warn(e, "Exception when trying link role to new user with id: ", userId);
            throw new RuntimeException("Failed to insert", e);
        } finally {
            session.close();
        }
    }

    public void linkRoleToUser(long roleId, long userId) {
        final List<Role> userRoles = findByUserId(userId);
        for(Role r : userRoles) {
            if(r.getId() == roleId) {
                // already linked
                return;
            }
        }
        linkRoleToNewUser(roleId, userId);
    }

    public Role findRoleByName(final String name) {
        final List<Role> userRoles = findAll();
        for(Role r : userRoles) {
            if(r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }

    public Map<String, Role> getExternalRolesMapping(String type) {
        if(type == null) {
            type = "";
        }
        final SqlSession session = factory.openSession();
        List<Object> roleMappingList = null;
        try {
            log.debug("Getting external roles mapping for type: ", type);
            final RolesMapper mapper = session.getMapper(RolesMapper.class);
            roleMappingList = mapper.getExternalRolesMapping(type);
            log.debug("Found external roles mappings: ", roleMappingList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to find external roles mapping with type: ", type);
        } finally {
            session.close();
        }

        final Map<String, Role> mapping = new HashMap<String, Role>();
        for(Object obj : roleMappingList) {
            final Map<String, Object> result = (Map<String, Object>) obj;
            final String externalName = (String) result.get("ext");
            final Role role = new Role();
            role.setId((Integer) result.get("id"));
            role.setName((String) result.get("name"));
            mapping.put(externalName, role);
        }

       if(mapping.isEmpty()) {
            // fallback to Oskari roles if mappings not provided
            List<Role> roles = findAll();
            for(Role role: roles) {
                mapping.put(role.getName(), role);
            }
        }

        return mapping;
    }
}

package fi.nls.oskari.user;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MybatisRoleService {

    private static final Logger log = LogFactory.getLogger(MybatisRoleService.class);

    private SqlSessionFactory factory = null;

    public MybatisRoleService() {
        factory = initializeMyBatis(DatasourceHelper.getInstance().getDataSource());
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, Role.class);
        MyBatisHelper.addMappers(configuration, RolesMapper.class);
        return MyBatisHelper.build(configuration);
    }

    public List<Role> findAll(){
        final SqlSession session = factory.openSession();
        List<Role> roleList = null;
        try {
            log.debug("Getting all roles");
            final RolesMapper mapper = session.getMapper(RolesMapper.class);
            roleList =  mapper.findAll();
            if(roleList == null) {
                roleList = Collections.emptyList();
            }
            log.debug("Found roles: ", roleList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to load all roles");
        } finally {
            session.close();
        }
        return roleList;
    }

    public void deleteUsersRoles(long userId){
        final SqlSession session = factory.openSession();
        try {
            log.debug("Finding roles by user id: ", userId);
            final RolesMapper mapper = session.getMapper(RolesMapper.class);
            mapper.deleteUsersRoles(userId);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to delete roles with user id: ", userId);
        } finally {
            session.close();
        }
    }

    public void delete(long userId){
        final SqlSession session = factory.openSession();
        try {
            log.debug("Finding user by id: ", userId);
            final RolesMapper mapper = session.getMapper(RolesMapper.class);
            mapper.delete(userId);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to delete user with id: ", userId);
        } finally {
            session.close();
        }
    }

    public long insert(Role role){
        final SqlSession session = factory.openSession();
        try {
            log.debug("Inserting role: ", role);
            final RolesMapper mapper = session.getMapper(RolesMapper.class);
            mapper.insert(role);
            session.commit();
            log.debug("Inserted role: ", role);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to insert role: ", role);
        } finally {
            session.close();
        }
        return role.getId();
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
        if(roleList == null || roleList.isEmpty()) {
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
            mapper.linkRoleToNewUser(roleId, userId);
            session.commit();
            log.debug("Linked role to new user with id: ", userId);
        } catch (Exception e) {
            log.warn(e, "Exception when trying link role to new user with id: ", userId);
            throw new ServiceRuntimeException("Failed to insert", e);
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
        final Map<String, Role> mapping = new HashMap<>();
        if(type == null) {
            type="";
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

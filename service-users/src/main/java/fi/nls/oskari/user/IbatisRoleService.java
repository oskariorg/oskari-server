package fi.nls.oskari.user;

import com.ibatis.sqlmap.client.SqlMapClient;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IbatisRoleService extends BaseIbatisService<Role> {
    @Override
    protected String getNameSpace() {
        return "Roles";
    }

    public List<Role> findByUserName(String username) {
        return queryForList(getNameSpace() + ".findByUserName", username);
    }
    public List<Role> findByUserId(long userId) {
        return queryForList(getNameSpace() + ".findByUserId", userId);
    }

    public Role findGuestRole() {
        List<Role> guestRoles = queryForList(getNameSpace() + ".findGuestRoles");
        if(guestRoles.isEmpty()) return null;
        return guestRoles.get(0);
    }

    /**
     * Same as linkRoleToUser except skips check if user has role already
     * @param roleId
     * @param userId
     */
    public void linkRoleToNewUser(long roleId, long userId) {

        final Map<String, Long> params = new HashMap<String, Long>();
        params.put("role_id", roleId);
        params.put("user_id", userId);
        try {
            getSqlMapClient().insert(getNameSpace() + ".linkRoleToUser", params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert", e);
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

    public Map<String, Role> getExternalRolesMapping(String type) {
        if(type == null) {
            type = "";
        }
        final List<Object> mappingList = queryForRawList(getNameSpace() + ".findExternalRolesOfType", type);
        final Map<String, Role> mapping = new HashMap<String, Role>();
        for(Object obj : mappingList) {
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

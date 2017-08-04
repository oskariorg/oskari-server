package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;

import java.util.List;
import java.util.Map;

public interface RolesMapper {
    List<Role> findByUserName(String username);
    List<Role> findByUserId(long userId);
    List<Role> findGuestRoles();
    void linkRoleToNewUser(Map<String,Long> params);
    List<Object> getExternalRolesMapping(String type);
}

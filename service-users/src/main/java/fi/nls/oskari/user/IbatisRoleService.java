package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.util.List;

public class IbatisRoleService extends BaseIbatisService<Role> {
    @Override
    protected String getNameSpace() {
        return "Roles";
    }

    public List<Role> findByUserName(String username) {
        return queryForList(getNameSpace() + ".findByUserName", username);
    }

    public Role findGuestRole() {
        List<Role> guestRoles = queryForList(getNameSpace() + ".findGuestRoles");
        if(guestRoles.isEmpty()) return null;
        return guestRoles.get(0);
    }
}

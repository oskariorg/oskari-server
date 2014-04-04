package fi.nls.oskari.user;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.service.db.BaseIbatisService;

public class IbatisRoleService extends BaseIbatisService<Role> {
    @Override
    protected String getNameSpace() {
        return "Roles";
    }
}

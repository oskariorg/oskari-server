package fi.nls.oskari.user;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari
public class UserContentRoleService extends UserContentService {

    private IbatisRoleService roleService = new IbatisRoleService();

    public void deleteUserContent(User user) throws ServiceException {
        roleService.deleteUsersRoles(user.getId());
    }
}
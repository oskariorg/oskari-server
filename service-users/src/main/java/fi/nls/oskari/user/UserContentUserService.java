package fi.nls.oskari.user;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari
public class UserContentUserService extends UserContentService {

    private IbatisUserService userService = new IbatisUserService();

    public void deleteUserContent(User user) throws ServiceException {
        userService.deletePassword(user.getScreenname());
        userService.delete(user.getId());
    }
}
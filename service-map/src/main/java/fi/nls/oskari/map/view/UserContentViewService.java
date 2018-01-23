package fi.nls.oskari.map.view;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari
public class UserContentViewService extends UserContentService {

    private ViewServiceIbatisImpl viewService;

    public void deleteUserContent(User user) throws ServiceException {
        if (viewService == null) {
            viewService = new ViewServiceIbatisImpl();
        }
        try {
            viewService.deleteViewByUserId(user.getId());
        }
        catch (ViewException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
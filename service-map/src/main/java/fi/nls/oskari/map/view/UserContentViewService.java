package fi.nls.oskari.map.view;

import fi.nls.oskari.annotation.Oskari;
import org.oskari.user.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari
public class UserContentViewService extends UserContentService {

    private AppSetupServiceMybatisImpl viewService;

    public void deleteUserContent(User user) throws ServiceException {
        if (viewService == null) {
            viewService = new AppSetupServiceMybatisImpl();
        }
        try {
            viewService.deleteViewByUserId(user.getId());
        }
        catch (ViewException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
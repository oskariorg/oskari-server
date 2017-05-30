
package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari
public class UserContentIndicatorService extends UserContentService {

    private UserIndicatorServiceImpl indicatorService = new UserIndicatorServiceImpl();

    public void deleteUserContent(User user) throws ServiceException {
        indicatorService.deleteByUserId(user.getId());
    }
}
package fi.nls.oskari.myplaces;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari
public class UserContentMyPlacesService extends UserContentService {

    private MyPlacesServiceMybatisImpl myPlacesService = new MyPlacesServiceMybatisImpl();

    public void deleteUserContent(User user) throws ServiceException {
        myPlacesService.deleteByUid(user.getUuid());
    }
}
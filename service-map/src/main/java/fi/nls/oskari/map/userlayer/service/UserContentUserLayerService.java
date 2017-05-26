package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari
public class UserContentUserLayerService extends UserContentService {

    private UserLayerDbServiceIbatisImpl userLayerService = new UserLayerDbServiceIbatisImpl();

    public void deleteUserContent(User user) throws ServiceException {
        userLayerService.deleteUserLayerByUid(user.getUuid());
    }
}
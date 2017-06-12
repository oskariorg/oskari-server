package fi.nls.oskari.map.userlayer.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari("userlayer")
public class UserContentUserLayerService extends UserContentService {

    private UserLayerDbServiceIbatisImpl userLayerService = null;

    @Override
    public void init() {
        super.init();
        if(DatasourceHelper.isModuleEnabled(getName())) {
            userLayerService = new UserLayerDbServiceIbatisImpl();
        }
    }

    public void deleteUserContent(User user) throws ServiceException {
        if(!DatasourceHelper.isModuleEnabled(getName())) {
            return;
        }
        userLayerService.deleteUserLayerByUid(user.getUuid());
    }
}
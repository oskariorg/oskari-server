package fi.nls.oskari.myplaces;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari("myplaces")
public class UserContentMyPlacesService extends UserContentService {

    private MyPlacesServiceMybatisImpl myPlacesService = null;

    @Override
    public void init() {
        super.init();
        if(DatasourceHelper.isModuleEnabled(getName())) {
            myPlacesService = new MyPlacesServiceMybatisImpl();
        }
    }

    public void deleteUserContent(User user) throws ServiceException {
        if(!DatasourceHelper.isModuleEnabled(getName())) {
            return;
        }
        myPlacesService.deleteByUid(user.getUuid());
    }
}
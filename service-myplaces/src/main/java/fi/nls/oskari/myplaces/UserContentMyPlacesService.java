package fi.nls.oskari.myplaces;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import org.oskari.user.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari("myplaces")
public class UserContentMyPlacesService extends UserContentService {

    private MyPlacesServiceMybatisImpl myPlacesService = null;

    /**
     * Cache key for my places place
     * This was in MyPlacesServiceMybatisImpl, but it requires too much mocking for static variable services in tests...
     * @param id
     * @return
     */
    public static String getPlaceCacheKey(long id) {
        return Long.toString(id);
    }
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
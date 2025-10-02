package org.oskari.map.myfeatures.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import org.oskari.user.User;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari("myfeatures")
public class UserContentMyFeaturesService extends UserContentService {
    
    public void deleteUserContent(User user) throws ServiceException {
        if(!DatasourceHelper.isModuleEnabled(getName())) {
            return;
        }
        UserLayerDbService userLayerService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
        userLayerService.deleteUserLayersByUuid(user.getUuid());
    }
}
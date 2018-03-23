package org.oskari.map.userlayer.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari("userlayer")
public class UserContentUserLayerService extends UserContentService {
    
    public void deleteUserContent(User user) throws ServiceException {
        if(!DatasourceHelper.isModuleEnabled(getName())) {
            return;
        }
        UserLayerDbService userLayerService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
        userLayerService.deleteUserLayersByUuid(user.getUuid());
    }
}
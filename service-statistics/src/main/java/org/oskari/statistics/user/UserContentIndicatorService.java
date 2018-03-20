
package org.oskari.statistics.user;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.UserContentService;

@Oskari
public class UserContentIndicatorService extends UserContentService {

    private StatisticalIndicatorService indicatorService;

    public void deleteUserContent(User user) throws ServiceException {
        if(indicatorService == null) {
            indicatorService = OskariComponentManager.getComponentOfType(StatisticalIndicatorService.class);
        }
        indicatorService.deleteByUser(user.getId());
    }
}
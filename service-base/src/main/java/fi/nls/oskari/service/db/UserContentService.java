package fi.nls.oskari.service.db;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceException;

public abstract class UserContentService extends OskariComponent {
    public abstract void deleteUserContent(User user) throws ServiceException;
}
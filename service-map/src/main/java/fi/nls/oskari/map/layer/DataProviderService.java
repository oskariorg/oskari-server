package fi.nls.oskari.map.layer;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.service.db.BaseService;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 16.12.2013
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public interface DataProviderService extends BaseService<DataProvider> {
    public boolean hasPermissionToUpdate(final User user, final int layerId);
    public DataProvider findByName(final String name);
}

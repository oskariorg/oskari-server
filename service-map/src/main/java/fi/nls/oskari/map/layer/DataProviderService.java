package fi.nls.oskari.map.layer;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.service.OskariComponent;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 16.12.2013
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public abstract class DataProviderService extends OskariComponent {
    public abstract boolean hasPermissionToUpdate(final User user, final int layerId);
    public abstract DataProvider findByName(final String name);
    public abstract DataProvider find(int id);
    public abstract List<DataProvider> findAll();
    public abstract void delete(int id);
    public abstract void update(DataProvider dataProvider);
    public abstract int insert(DataProvider dataProvider);

}

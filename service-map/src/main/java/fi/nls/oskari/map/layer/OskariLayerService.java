package fi.nls.oskari.map.layer;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 16.12.2013
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public interface OskariLayerService extends BaseService<OskariLayer> {

    public List<OskariLayer> find(final List<String> idList);

    public boolean hasPermissionToUpdate(final User user, final int layerId);
}

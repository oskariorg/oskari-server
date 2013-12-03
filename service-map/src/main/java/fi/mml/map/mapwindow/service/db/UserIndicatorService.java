package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.indicator.UserIndicator;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: EVAARASMAKI
 * Date: 22.11.2013
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */

public interface UserIndicatorService extends BaseService<UserIndicator> {

    public List<UserIndicator> findAllOfUser(long userID);
}

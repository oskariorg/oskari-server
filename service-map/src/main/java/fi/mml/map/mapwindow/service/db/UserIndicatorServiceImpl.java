package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.indicator.UserIndicator;
import fi.nls.oskari.service.db.BaseIbatisService;

/**
 * Created with IntelliJ IDEA.
 * User: EVAARASMAKI
 * Date: 22.11.2013
 * Time: 13:50
 * To change this template use File | Settings | File Templates.
 */
public class UserIndicatorServiceImpl extends BaseIbatisService<UserIndicator> implements UserIndicatorService {
    @Override
    protected String getNameSpace() {
        return "UserIndicator";
    }
}

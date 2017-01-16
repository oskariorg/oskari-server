package fi.nls.oskari.control.statistics.plugins.user;

import fi.mml.map.mapwindow.service.db.UserIndicatorService;
import fi.mml.map.mapwindow.service.db.UserIndicatorServiceImpl;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.indicator.UserIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.ArrayList;
import java.util.List;

public class UserIndicatorsStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(UserIndicatorsStatisticalDatasourcePlugin.class);
    private static UserIndicatorService userIndicatorService = new UserIndicatorServiceImpl();

    public UserIndicatorsStatisticalDatasourcePlugin() {
    }

    @Override
    public List<StatisticalIndicator> getIndicators(User user) {
        // Getting the general information of all the indicator layers.
        if (user == null) {
            return new ArrayList<>();
        }
        long uid = user.getId();
        List<UserIndicator> userIndicators = userIndicatorService.findAllOfUser(uid);
        return toUserStatisticalIndicators(userIndicators);
    }

    private List<StatisticalIndicator> toUserStatisticalIndicators(List<UserIndicator> userIndicators) {
        List<StatisticalIndicator> indicators = new ArrayList<>();
        for (UserIndicator userIndicator : userIndicators) {
            indicators.add(new UserStatisticalIndicator(userIndicator));
        }
        return indicators;
    }

    @Override
    public boolean canCache() {
        // Because the results are based on the user doing the query, we should not cache here.
        return false;
    }
}

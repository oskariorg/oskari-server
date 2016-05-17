package fi.nls.oskari.control.statistics.plugins.user;

import java.util.ArrayList;
import java.util.List;

import fi.mml.map.mapwindow.service.db.UserIndicatorService;
import fi.mml.map.mapwindow.service.db.UserIndicatorServiceImpl;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.indicator.UserIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import org.json.JSONObject;

public class UserIndicatorsStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(UserIndicatorsStatisticalDatasourcePlugin.class);
    private static UserIndicatorService userIndicatorService = new UserIndicatorServiceImpl();

    public UserIndicatorsStatisticalDatasourcePlugin() {
    }

    @Override
    public List<? extends StatisticalIndicator> getIndicators(User user) {
        // Getting the general information of all the indicator layers.
        if (user == null) {
            return new ArrayList<>();
        }
        long uid = user.getId();
        List<UserIndicator> userIndicators = userIndicatorService.findAllOfUser(uid);
        return toUserStatisticalIndicators(userIndicators);
    }

    private List<? extends StatisticalIndicator> toUserStatisticalIndicators(List<UserIndicator> userIndicators) {
        List<UserStatisticalIndicator> indicators = new ArrayList<>();
        for (UserIndicator userIndicator : userIndicators) {
            indicators.add(new UserStatisticalIndicator(userIndicator));
        }
        return indicators;
    }

    @Override
    public void init(StatisticalDatasource source) {
    }

    @Override
    public boolean canCache() {
        // Because the results are based on the user doing the query, we should not cache here.
        return false;
    }
}

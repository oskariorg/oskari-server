package fi.nls.oskari.control.statistics.plugins;

import java.util.List;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.domain.User;

public class MockPlugin extends StatisticalDatasourcePlugin {

    @Override
    public List<StatisticalIndicator> getIndicators(User user) {
        return null;
    }

    @Override
    public boolean canCache() {
        return false;
    }

}

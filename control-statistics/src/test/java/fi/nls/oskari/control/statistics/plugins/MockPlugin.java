package fi.nls.oskari.control.statistics.plugins;

import java.util.List;

import fi.nls.oskari.domain.User;

public class MockPlugin implements StatisticalDatasourcePlugin {

    @Override
    public List<StatisticalIndicator> getIndicators(User user) {
        return null;
    }

    @Override
    public void init() {
    }

    @Override
    public boolean canCache() {
        return false;
    }

}

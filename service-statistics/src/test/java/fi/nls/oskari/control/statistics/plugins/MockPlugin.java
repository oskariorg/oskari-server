package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.data.IndicatorSet;
import fi.nls.oskari.domain.User;

public class MockPlugin extends StatisticalDatasourcePlugin {

    @Override
    public IndicatorSet getIndicatorSet(User user) {
        return new IndicatorSet();
    }

    @Override
    public void update() {
    }

    @Override
    public boolean canCache() {
        return false;
    }

}

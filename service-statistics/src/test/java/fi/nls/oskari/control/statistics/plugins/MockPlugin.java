package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.domain.User;

import java.util.Map;

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
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicator indicator, StatisticalIndicatorDataModel params, StatisticalIndicatorLayer regionset) {
        return null;
    }

}

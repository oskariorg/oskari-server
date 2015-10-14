package fi.nls.oskari.control.statistics.plugins;

import java.util.List;

public class MockPlugin implements StatisticalDatasourcePlugin {

    @Override
    public List<StatisticalIndicator> getIndicators() {
        return null;
    }

    @Override
    public void init() {
    }

}

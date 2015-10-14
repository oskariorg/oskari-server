package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;

public class SotkaStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private SotkaRegionParser sotkaParser = null;

    @Override
    public List<StatisticalIndicator> getIndicators() {
        // TODO: Implement this.
        return new ArrayList<>();
    }

    @Override
    public void init() {
        // Note that initialization of the parser is not synchronous and not instant.
        // TODO: Use Futures to prevent a race condition.
        sotkaParser = new SotkaRegionParser();
    }

}

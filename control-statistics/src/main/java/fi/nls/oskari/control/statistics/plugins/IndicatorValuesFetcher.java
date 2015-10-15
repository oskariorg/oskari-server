package fi.nls.oskari.control.statistics.plugins;

import java.util.Map;

public interface IndicatorValuesFetcher {

    public Map<String, IndicatorValue> get(StatisticalIndicatorSelectors selectors);
}

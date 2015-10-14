package fi.nls.oskari.control.statistics.plugins;

import java.util.Map;

public interface IndicatorValuesFetcher {

    Map<String, IndicatorValue> get(StatisticalIndicatorSelectors selectors);

}

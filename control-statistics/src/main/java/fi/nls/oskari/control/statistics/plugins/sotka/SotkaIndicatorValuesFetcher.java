package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValuesFetcher;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;

/**
 * This fetches the indicator value tables transparently from Sotka.
 * We don't want to make a separate call to the plugin interface for this, because some
 * APIs / plugins might give all the information in the same response, or divide and key the responses differently.
 */
public class SotkaIndicatorValuesFetcher implements IndicatorValuesFetcher {
    @Override
    public Map<String, IndicatorValue> get(StatisticalIndicatorSelectors selectors) {
        // TODO: Implement. Fetch the CSV and parse.
        return null;
    }

}

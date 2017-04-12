package fi.nls.oskari.control.statistics.data;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;

import java.util.Collections;
import java.util.List;

/**
 * Indicator listing from a single datasource including status information about processing available indicators.
 */
public class IndicatorSet {

    private boolean isComplete = false;

    List<StatisticalIndicator> indicators;

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public List<StatisticalIndicator> getIndicators() {
        if(indicators == null) {
            return Collections.emptyList();
        }
        return indicators;
    }

    public void setIndicators(
            List<StatisticalIndicator> indicators) {
        this.indicators = indicators;
    }
}

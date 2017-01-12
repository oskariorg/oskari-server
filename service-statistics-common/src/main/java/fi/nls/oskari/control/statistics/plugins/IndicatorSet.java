package fi.nls.oskari.control.statistics.plugins;

import java.util.Collections;
import java.util.List;

/**
 * Indicator listing from a single datasource including status information about processing available indicators.
 */
public class IndicatorSet {

    private boolean isComplete = false;

    List<? extends StatisticalIndicator> indicators;

    public boolean isComplete() {
        return isComplete;
    }

    void setComplete(boolean complete) {
        isComplete = complete;
    }

    public List<? extends StatisticalIndicator> getIndicators() {
        if(indicators == null) {
            return Collections.emptyList();
        }
        return indicators;
    }

    void setIndicators(
            List<? extends StatisticalIndicator> indicators) {
        this.indicators = indicators;
    }
}

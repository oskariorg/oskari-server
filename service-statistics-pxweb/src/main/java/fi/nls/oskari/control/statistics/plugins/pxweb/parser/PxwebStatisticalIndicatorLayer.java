package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;

import java.util.Map;

public class PxwebStatisticalIndicatorLayer implements StatisticalIndicatorLayer {
    @Override
    public long getOskariLayerId() {
        return 0;
    }

    @Override
    public IndicatorValueType getIndicatorValueType() {
        return null;
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorSelectors selectors) {
        return null;
    }
}

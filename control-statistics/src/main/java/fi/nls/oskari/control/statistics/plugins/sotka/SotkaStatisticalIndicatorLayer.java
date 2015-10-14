package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.control.statistics.plugins.IndicatorValuesFetcher;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;

public class SotkaStatisticalIndicatorLayer implements StatisticalIndicatorLayer {
    private String id;
    private IndicatorValueType valueType;
    private IndicatorValuesFetcher indicatorValuesFetcher;
    
    public SotkaStatisticalIndicatorLayer(String id, IndicatorValueType valueType,
            IndicatorValuesFetcher indicatorValuesFetcher) {
        this.id = id;
        this.valueType = valueType;
        this.indicatorValuesFetcher = indicatorValuesFetcher;
    }
    
    @Override
    public String getOskariMapLayerId() {
        return id;
    }

    @Override
    public String getOskariMapLayerVersion() {
        // FIXME: How can we determine the Oskari map layers version this API gives its responses against? From db?
        return null;
    }

    @Override
    public IndicatorValueType getIndicatorValueType() {
        return valueType;
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorSelectors selectors) {
        return indicatorValuesFetcher.get(selectors);
    }

}

package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaIndicatorValuesFetcher;

public class SotkaStatisticalIndicatorLayer implements StatisticalIndicatorLayer {
    private String sotkaId;
    private long oskariId;
    private String indicatorId;
    private IndicatorValueType valueType;
    private SotkaIndicatorValuesFetcher indicatorValuesFetcher;
    
    public SotkaStatisticalIndicatorLayer(String sotkaId, long oskariId, IndicatorValueType valueType,
            SotkaIndicatorValuesFetcher indicatorValuesFetcher,
            String indicatorId) {
        this.sotkaId = sotkaId;
        this.oskariId = oskariId;
        this.valueType = valueType;
        this.indicatorValuesFetcher = indicatorValuesFetcher;
        this.indicatorId = indicatorId;
    }
    
    @Override
    public IndicatorValueType getIndicatorValueType() {
        return valueType;
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorSelectors selectors) {
        return indicatorValuesFetcher.get(selectors, this.indicatorId, this.sotkaId);
    }

    @Override
    public String toString() {
        return "{id: " + oskariId + ", valueType: " + valueType + "}";
    }

    @Override
    public long getOskariLayerId() {
        return oskariId;
    }
}

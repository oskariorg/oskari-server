package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaIndicatorValuesFetcher;

public class SotkaStatisticalIndicatorLayer implements StatisticalIndicatorLayer {
    private String id;
    private String indicatorId;
    private IndicatorValueType valueType;
    private SotkaIndicatorValuesFetcher indicatorValuesFetcher;
    
    public SotkaStatisticalIndicatorLayer(String id, IndicatorValueType valueType,
            SotkaIndicatorValuesFetcher indicatorValuesFetcher, String indicatorId) {
        this.id = id;
        this.valueType = valueType;
        this.indicatorValuesFetcher = indicatorValuesFetcher;
        this.indicatorId = indicatorId;
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
        return indicatorValuesFetcher.get(selectors, this.indicatorId, this.id);
    }

    @Override
    public String toString() {
        return "{id: " + id + ", valueType: " + valueType + "}";
    }
}

package fi.nls.oskari.control.statistics.plugins.kapa.parser;

import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.kapa.KapaIndicatorValuesFetcher;

public class KapaStatisticalIndicatorLayer implements StatisticalIndicatorLayer {
    private String id;
    private String indicatorId;
    private IndicatorValueType valueType;
    private KapaIndicatorValuesFetcher indicatorValuesFetcher;
    
    public KapaStatisticalIndicatorLayer(String id, IndicatorValueType valueType,
            KapaIndicatorValuesFetcher indicatorValuesFetcher, String indicatorId) {
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
        // The layer versioning starts with "1", and is incremented when the Oskari layers are updated and
        // incremented here when the plugin data source starts reflecting the newly published layers.
        return "1";
    }

    @Override
    public IndicatorValueType getIndicatorValueType() {
        return valueType;
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorSelectors selectors) {
        return indicatorValuesFetcher.get(selectors, this.indicatorId);
    }

    @Override
    public String toString() {
        return "{id: " + id + ", valueType: " + valueType + "}";
    }
}

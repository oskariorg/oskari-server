package fi.nls.oskari.control.statistics.plugins.kapa.parser;

import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.kapa.KapaIndicatorValuesFetcher;

public class KapaStatisticalIndicatorLayer implements StatisticalIndicatorLayer {
    private String indicatorId;
    private IndicatorValueType valueType;
    private KapaIndicatorValuesFetcher indicatorValuesFetcher;
    private long layerId;
    
    public KapaStatisticalIndicatorLayer(long layerId,
            IndicatorValueType valueType,
            KapaIndicatorValuesFetcher indicatorValuesFetcher,
            String indicatorId) {
        this.layerId = layerId;
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
        return indicatorValuesFetcher.get(selectors, this.indicatorId);
    }

    @Override
    public long getOskariLayerId() {
        return layerId;
    }
}

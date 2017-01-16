package fi.nls.oskari.control.statistics.plugins.kapa.parser;

import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.IndicatorValueType;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.control.statistics.plugins.kapa.KapaIndicatorValuesFetcher;

public class KapaStatisticalIndicatorLayer extends StatisticalIndicatorLayer {
    private IndicatorValueType valueType;
    private KapaIndicatorValuesFetcher indicatorValuesFetcher;
    
    public KapaStatisticalIndicatorLayer(long layerId,
                                         String indicatorId,
            IndicatorValueType valueType,
            KapaIndicatorValuesFetcher indicatorValuesFetcher) {
        super(layerId, indicatorId);
        this.valueType = valueType;
        this.indicatorValuesFetcher = indicatorValuesFetcher;
    }
    
    @Override
    public IndicatorValueType getIndicatorValueType() {
        return valueType;
    }

    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorSelectors selectors) {
        return indicatorValuesFetcher.get(selectors, getIndicatorId());
    }

}

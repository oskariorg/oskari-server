package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import java.util.Map;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaIndicatorValuesFetcher;

public class SotkaStatisticalIndicatorLayer extends StatisticalIndicatorLayer {
    private String sotkaId;
    private SotkaIndicatorValuesFetcher indicatorValuesFetcher;
    
    public SotkaStatisticalIndicatorLayer(long oskariId, String indicatorId, String sotkaId,
            SotkaIndicatorValuesFetcher indicatorValuesFetcher) {
        super(oskariId, indicatorId);
        this.sotkaId = sotkaId;
        this.indicatorValuesFetcher = indicatorValuesFetcher;
    }


    @Override
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorDataModel selectors) {
        return indicatorValuesFetcher.get(selectors, getIndicatorId(), this.sotkaId);
    }


}

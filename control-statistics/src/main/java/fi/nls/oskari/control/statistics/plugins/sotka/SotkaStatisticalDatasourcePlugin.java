package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaRegionParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.Indicators;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;

public class SotkaStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private SotkaRegionParser sotkaParser = null;

    @Override
    public List<StatisticalIndicator> getIndicators() throws ActionException {
        SotkaRequest request = SotkaRequest.getInstance(Indicators.NAME);
        String jsonResponse = request.getData();
        
        return new ArrayList<>();
    }

    @Override
    public void init() {
        sotkaParser = new SotkaRegionParser();
    }

}

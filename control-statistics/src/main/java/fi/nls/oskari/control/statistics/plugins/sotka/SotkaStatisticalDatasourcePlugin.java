package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.List;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicatorsParser;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaRegionParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.Indicators;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;

public class SotkaStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private SotkaRegionParser regionParser = null;
    private SotkaIndicatorsParser indicatorsParser = null;

    @Override
    public List<? extends StatisticalIndicator> getIndicators() {
        SotkaRequest request = SotkaRequest.getInstance(Indicators.NAME);
        String jsonResponse;
        try {
            jsonResponse = request.getData();
        } catch (ActionException e) {
            throw new APIException("Something went wrong calling SotkaNET Indicators interface.", e);
        }
        return indicatorsParser.parse(jsonResponse);
    }

    @Override
    public void init() {
        regionParser = new SotkaRegionParser();
        indicatorsParser = new SotkaIndicatorsParser();
    }

}

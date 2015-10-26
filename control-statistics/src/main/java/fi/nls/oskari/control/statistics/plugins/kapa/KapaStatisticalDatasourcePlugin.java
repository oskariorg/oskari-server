package fi.nls.oskari.control.statistics.plugins.kapa;

import java.util.List;

import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.kapa.parser.KapaIndicator;
import fi.nls.oskari.control.statistics.plugins.kapa.parser.KapaIndicatorsParser;
import fi.nls.oskari.control.statistics.plugins.kapa.requests.KapaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class KapaStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(KapaStatisticalDatasourcePlugin.class);
    private KapaIndicatorsParser indicatorsParser;
    
    public KapaStatisticalDatasourcePlugin() {
        indicatorsParser = new KapaIndicatorsParser();
    }

    @Override
    public List<? extends StatisticalIndicator> getIndicators() {
        // Getting the general information of all the indicator layers.
        KapaRequest request = new KapaRequest();
        String jsonResponse = request.getIndicators();
        List<KapaIndicator> indicators = indicatorsParser.parse(jsonResponse);
        return indicators;
    }

    @Override
    public void init() {
    }
}

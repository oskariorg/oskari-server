package fi.nls.oskari.control.statistics.plugins.pxweb;

import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.control.statistics.plugins.pxweb.parser.PxwebIndicatorsParser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.Collections;
import java.util.List;

public class PxwebStatisticalDatasourcePlugin extends StatisticalDatasourcePlugin {
    private PxwebIndicatorsParser indicatorsParser;

    private PxwebConfig config;

    @Override
    public void update() {
        List<StatisticalIndicator> indicators = indicatorsParser.parse(getSource().getLayers());
        for(StatisticalIndicator ind: indicators) {
            onIndicatorProcessed(ind);
        }
    }

    @Override
    public void init(StatisticalDatasource source) {
        super.init(source);
        config = new PxwebConfig(source.getConfigJSON(), source.getId());
        indicatorsParser = new PxwebIndicatorsParser(config);
    }
}

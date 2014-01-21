package fi.nls.oskari.map.stats;

import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import java.util.List;

public class VisualizationServiceIbatisImpl extends VisualizationService {
    
    private static final Logger log = LogFactory.getLogger(VisualizationServiceIbatisImpl.class);

    @Override
    protected String getNameSpace() {
        return "StatsVisualization";
    }

    public List<StatsVisualization> findForLayerId(
            final int layerId) {
        final List<StatsVisualization> visualizations = queryForList(
                getNameSpace() + ".findForLayerId", layerId);
        return visualizations;
    }
}

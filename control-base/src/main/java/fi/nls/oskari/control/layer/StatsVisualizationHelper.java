package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ServiceFactory;

import java.util.List;

public class StatsVisualizationHelper {

    private static final Logger log = LogFactory.getLogger(StatsVisualizationHelper.class);


    private final MapLayerService mapLayerService = ServiceFactory
            .getMapLayerService();


    public StatsVisualization getVisualization(
            final int statsLayerId,
            final int visId,
            final String classes,
            final String layerName,
            final String filterProperty,
            final String vis
    ) {
        if (statsLayerId == -1) {
            return null;
        }

        if (visId != -1) {
            // got id -> find from DB
            return getVisualizationFromDB(statsLayerId, visId);
        }

        // else we are expecting parameters to construct a visualization
        final StatsVisualization visualization = new StatsVisualization();
        visualization.setClasses(classes);
        visualization.setLayername(layerName);
        visualization.setFilterproperty(filterProperty);
        // vis=<visualization>:<colors>
        final String[] colors = vis.split(":");
        if (colors.length > 1) {
            visualization.setVisualization(colors[0]);
            visualization.setColors(colors[1]);
        }
        // validate that we got all needed params
        if (visualization.isValid()) {
            return visualization;
        }

        log.warn("Tried to create StatsVisualization but param values were not valid:",
                visualization);
        return null;
    }

    private StatsVisualization getVisualizationFromDB(final int statsLayerId,
                                                      final int visId) {
        // TODO: we should just get the visualization by id
        final List<StatsVisualization> visualizations = mapLayerService
                .findStatsLayerVisualizations(statsLayerId);
        for (StatsVisualization vis : visualizations) {
            if (visId == vis.getId()) {
                return vis;
            }
        }
        return null;

    }

}

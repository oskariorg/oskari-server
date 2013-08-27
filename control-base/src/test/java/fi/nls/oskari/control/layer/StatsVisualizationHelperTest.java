package fi.nls.oskari.control.layer;

import fi.nls.oskari.domain.map.stats.StatsVisualization;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 16.4.2013
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
public class StatsVisualizationHelperTest {
    final StatsVisualizationHelper helper = new StatsVisualizationHelper();

    @Test
    public void testGetVisualization() throws Exception {
        final StatsVisualization nullVis = helper.getVisualization(
                -1, -1, "classes", "layerName", "filterProperty", "vis");
        assertNull("Visualization without layer id should be null", nullVis);


        final StatsVisualization corruptedVis = helper.getVisualization(
                276, -1, "classes,id1|toomanyclasses|third_group", "layerName", "filterProperty", "choro:3groups|only2colors");
        assertNull("Visualization with different amount of groups ans colors should be null", corruptedVis);

        final StatsVisualization layerVis = helper.getVisualization(
                276, -1, "classes", "layerName", "filterProperty", "choro:color");
        assertNotNull("Visualization with layer id should be null", layerVis);
    }
}

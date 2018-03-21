package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.stats.VisualizationService;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatterStats extends LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterStats.class);
    private final VisualizationService service = new VisualizationService();

    public JSONObject getJSON(OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure,
                                     final String crs) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);

        // get visualizations for statslayers
        final List<StatsVisualization> list = service.findForLayerId(layer.getId());
        final JSONArray visualizationList = new JSONArray();
        for(StatsVisualization vis : list) {
            final JSONObject visualization = new JSONObject();
            JSONHelper.putValue(visualization, "id", vis.getId());
            JSONHelper.putValue(visualization, "name", vis.getName(lang));
            JSONHelper.putValue(visualization, "filterproperty", vis.getFilterproperty());
            visualizationList.put(visualization);
        }
        JSONHelper.putValue(layerJson, "visualizations", visualizationList);

        return layerJson;
    }

}

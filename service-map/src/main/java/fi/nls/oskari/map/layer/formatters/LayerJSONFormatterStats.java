package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.stats.StatsLayer;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatterStats extends LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterStats.class);

    public JSONObject getJSON(OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure);

        // TODO: fix visualization for statslayers
/*
        StatsLayer statsLayer = (StatsLayer) layer;
        final JSONArray visualizationList = new JSONArray();
        for(StatsVisualization vis : statsLayer.getVisualizations()) {
            final JSONObject visualization = new JSONObject();
            JSONHelper.putValue(visualization, "id", vis.getId());
            JSONHelper.putValue(visualization, "name", vis.getName(lang));
            JSONHelper.putValue(visualization, "filterproperty", vis.getFilterproperty());
            visualizationList.put(visualization);
        }
        layerJson.put("visualizations", visualizationList);
        */

        return layerJson;
    }

}

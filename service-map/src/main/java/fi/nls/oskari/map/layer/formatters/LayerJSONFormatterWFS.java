package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatterWFS extends LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWFS.class);
    private static WFSLayerConfigurationService wfsService = new WFSLayerConfigurationServiceIbatisImpl();


    public JSONObject getJSON(OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure);
        JSONHelper.putValue(layerJson, "styles", getStyles(layer));
        JSONHelper.putValue(layerJson, "style", "default");
        JSONHelper.putValue(layerJson, "isQueryable", true);
        return layerJson;
    }

    /**
     * Constructs a style json
     *
     * @param layer layer of which styles will be retrieved
     */
    private JSONArray getStyles(final OskariLayer layer) {
        List<WFSSLDStyle> styleList = wfsService.findWFSLayerStyles(layer.getId());
        JSONArray arr = new JSONArray();
        for (WFSSLDStyle style : styleList) {
            JSONObject obj = createStylesJSON(style.getName(), style.getName(), style.getName());
            if(obj.length() > 0) {
                arr.put(obj);
            }
        }
        return arr;
    }
}

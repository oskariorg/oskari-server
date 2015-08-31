package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
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
public class LayerJSONFormatterWMTS extends LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWMTS.class);

    public JSONObject getJSON(OskariLayer layer,
                              final String lang,
                              final boolean isSecure) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure);
        JSONHelper.putValue(layerJson, "tileMatrixSetId", layer.getTileMatrixSetId());

        // TODO: parse tileMatrixSetData for styles and set default style name from the one where isDefault = true
        String styleName = layer.getStyle();

        if(styleName == null || styleName.isEmpty()) {
            styleName = "default";
        }
        JSONHelper.putValue(layerJson, "style", styleName);
        JSONArray styles = new JSONArray();
        // currently supporting only one style (default style)
        styles.put(createStylesJSON(styleName, styleName, null));
        JSONHelper.putValue(layerJson, "styles", styles);

        return layerJson;
    }

}

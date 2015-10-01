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

        // if options have urlTemplate -> use it (treat as a REST layer)
        final String urlTemplate = JSONHelper.getStringFromJSON(layer.getOptions(), "urlTemplate", null);
        final boolean needsProxy = useProxy(layer);
        if(urlTemplate != null) {
            if(needsProxy) {
                // remove requestEncoding so we always get KVP params when proxying
                JSONObject options = layerJson.optJSONObject("options");
                options.remove("requestEncoding");
            }
            else {
                // setup tileURL for REST layers
                final String originalUrl = layer.getUrl();
                layer.setUrl(urlTemplate);
                JSONHelper.putValue(layerJson, "tileUrl", layer.getUrl(isSecure));
                // switch back the original url in case it's used down the line
                layer.setUrl(originalUrl);
            }
        }
        return layerJson;
    }

}

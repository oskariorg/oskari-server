package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;
public class LayerJSONFormatterWFS extends LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWFS.class);

    public JSONObject getJSON(OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure,
                                     final String crs) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);
        JSONHelper.putValue(layerJson, KEY_ISQUERYABLE, true);
        // getBaseJSON adds these but model builder isn't using them. Frontend uses DescribeLayer response
        layerJson.remove(KEY_OPTIONS);
        layerJson.remove(KEY_ATTRIBUTES);
        return layerJson;
    }
}

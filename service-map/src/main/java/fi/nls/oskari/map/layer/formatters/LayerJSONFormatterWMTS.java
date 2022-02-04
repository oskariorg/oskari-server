package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

public class LayerJSONFormatterWMTS extends LayerJSONFormatter {

    private static final Logger LOG = LogFactory.getLogger(LayerJSONFormatterWMTS.class);

    public JSONObject getJSON(OskariLayer layer,
                              final String lang,
                              final boolean isSecure,
                              final String crs) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);
        try {
            JSONArray styles = createStylesJSON(layer, isSecure);
            JSONHelper.putValue(layerJson, KEY_STYLES, styles);
        } catch (Exception e) {
            LOG.warn(e, "Populating layer styles failed for id: " + layer.getId());
        }

        final boolean needsProxy = useProxy(layer);
        if (needsProxy || isBeingProxiedViaOskariServer(layerJson.optString("url"))) {
            // force requestEncoding so we always get KVP params when proxying
            JSONObject options = layerJson.optJSONObject("options");
            JSONHelper.putValue(options, "requestEncoding", "KVP");
        }

        Set<String> srs = getSRSs(layer.getAttributes(), layer.getCapabilities());
        if (srs != null) {
            JSONHelper.putValue(layerJson, KEY_SRS, new JSONArray(srs));
        }

        return layerJson;
    }

}

package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatter.class);
    // map different layer types for JSON formatting
    private static Map<String, LayerJSONFormatter> typeMapping = new HashMap<String, LayerJSONFormatter>();
    static {
        typeMapping.put(Layer.TYPE_WMS, new LayerJSONFormatterWMS());
        typeMapping.put(Layer.TYPE_WFS, new LayerJSONFormatterWFS());
        typeMapping.put(Layer.TYPE_WMTS, new LayerJSONFormatterWMTS());
        typeMapping.put(Layer.TYPE_STATS, new LayerJSONFormatterStats());
    }

    private static LayerJSONFormatter getFormatter(final String type) {
        if(type == null) {
            return null;
        }
        final LayerJSONFormatter formatter = typeMapping.get(type);
        if(formatter != null) {
            return formatter;
        }
        return null;
    }

    /**
     * Use to add custom layer JSON formatter for custom layer type
     * @param type
     * @param formatter
     */
    public static void addFormatter(final String type, final LayerJSONFormatter formatter) {
        typeMapping.put(type, formatter);
    }

    public JSONObject getJSON(final OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure) {
        LayerJSONFormatter formatter = getFormatter(layer.getType());
        // to prevent nullpointer and infinite loop
        if(formatter != null && !formatter.getClass().equals(LayerJSONFormatter.class)) {
            return formatter.getJSON(layer, lang, isSecure);
        }
        return getBaseJSON(layer, lang, isSecure);
    }

    public JSONObject getBaseJSON(final OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure) {
        JSONObject layerJson = new JSONObject();

        final String externalId = layer.getExternalId();
        if(externalId != null && !externalId.isEmpty()) {
            JSONHelper.putValue(layerJson, "id", externalId);
        }
        else {
            JSONHelper.putValue(layerJson, "id", layer.getId());
        }

        //log.debug("Type", layer.getType());
        if(layer.isCollection()) {
            // fixing frontend type for collection layers
            if(layer.isBaseMap()) {
                JSONHelper.putValue(layerJson, "type", "base");
            }
            else {
                JSONHelper.putValue(layerJson, "type", "groupMap");
            }
        }
        else {
            JSONHelper.putValue(layerJson, "type", layer.getType());
            //log.debug("wmsName", layer.getName());
            // for easier proxy routing on ssl hosts, maps all urls with prefix and a simplified url
            // so tiles can be fetched from same host from browsers p.o.v. and the actual url
            // is proxied with a proxy for example: /proxythis/<actual wmsurl>
            JSONHelper.putValue(layerJson, "wmsUrl", layer.getUrl(isSecure));
            JSONHelper.putValue(layerJson, "wmsName", layer.getName());
        }

        //log.debug("name", layer.getName(lang));
        JSONHelper.putValue(layerJson, "name", layer.getName(lang));
        //log.debug("subtitle", layer.getTitle(lang));
        JSONHelper.putValue(layerJson, "subtitle", layer.getTitle(lang));
        //log.debug("getGroup", layer.getGroup());
        if(layer.getGroup() != null) {
            JSONHelper.putValue(layerJson, "orgName", layer.getGroup().getName(lang));
        }
        //log.debug("getInspireTheme", layer.getInspireTheme());
        if(layer.getInspireTheme() != null) {
            JSONHelper.putValue(layerJson, "inspire", layer.getInspireTheme().getName(lang));
        }

        //log.debug("opacity", layer.getOpacity());
        if(layer.getOpacity() != null && layer.getOpacity() > -1 && layer.getOpacity() <= 100) {
            JSONHelper.putValue(layerJson, "opacity", layer.getOpacity());
        }
        if(layer.getMinScale() != null && layer.getMinScale() != -1) {
            JSONHelper.putValue(layerJson, "minScale", layer.getMinScale());
        }
        if(layer.getMaxScale() != null && layer.getMaxScale() != -1) {
            JSONHelper.putValue(layerJson, "maxScale", layer.getMaxScale());
        }
        JSONHelper.putValue(layerJson, "geom", layer.getGeometry());

        //log.debug("getLegendImage", layer.getLegendImage());
        JSONHelper.putValue(layerJson, "legendImage", layer.getLegendImage());
        JSONHelper.putValue(layerJson, "baseLayerId", layer.getParentId());

        //log.debug("getCreated", layer.getCreated());
        JSONHelper.putValue(layerJson, "created", layer.getCreated());
        JSONHelper.putValue(layerJson, "updated", layer.getUpdated());

        //log.debug("dataUrl_uuid", getFixedDataUrl(layer));
        JSONHelper.putValue(layerJson, "dataUrl_uuid", getFixedDataUrl(layer));

        // sublayer handling
        if(layer.getSublayers() != null && !layer.getSublayers().isEmpty()) {
            JSONArray sublayers = new JSONArray();
            for(OskariLayer sub : layer.getSublayers()) {
                JSONObject subJSON = getJSON(sub, lang, isSecure);
                sublayers.put(subJSON);
            }
            JSONHelper.putValue(layerJson, "subLayer", sublayers);
        }
        return layerJson;
    }


    public JSONObject createStylesJSON(String name, String title, String legend) {
        final JSONObject style = JSONHelper.createJSONObject("name", name);
        JSONHelper.putValue(style, "title", title);
        JSONHelper.putValue(style, "legend", legend);
        return style;
    }

    // This is solution of transition for dataUrl and for dataUrl_uuid
    private String getFixedDataUrl(final OskariLayer layer) {
        final String metadataId = layer.getMetadataId();
        if(metadataId == null || metadataId.isEmpty()) {
            return null;
        }
        //layerJson.put("dataUrl", metadataId);
        final int indexOf = metadataId.indexOf("uuid=");
        if (indexOf > 0) {
            // parse uuid from URL
            return metadataId.substring(indexOf + 5);
        }
        return metadataId;
    }
}

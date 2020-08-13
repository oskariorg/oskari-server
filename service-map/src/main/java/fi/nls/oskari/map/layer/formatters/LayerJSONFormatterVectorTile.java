package fi.nls.oskari.map.layer.formatters;

import org.json.JSONObject;

import fi.nls.oskari.domain.map.OskariLayer;

public class LayerJSONFormatterVectorTile extends LayerJSONFormatter {

    public static final String URL_PARAM_X = "x";
    public static final String URL_PARAM_Y = "y";
    public static final String URL_PARAM_Z = "z";

    @Override
    public JSONObject getJSON(OskariLayer layer, String lang, boolean isSecure, String crs) {
        return getBaseJSON(layer, lang, isSecure, crs);
    }

    @Override
    public String getProxyUrl(final OskariLayer layer) {
        // Little hack here to avoid IOHelper escaping query parameters
        // We don't want this:
        // "url": "/action?action_route=GetLayerTile&x=%7Bx%7D&y=%7By%7D&z=%7Bz%7D&id=2635",
        // Instead we want this:
        // "url": "/action?action_route=GetLayerTile&x={x}&y={y}&z={z}&id=2635
        // OpenLayers uses {x}, {y}, {z} as placeholders
        StringBuilder proxyUrl = new StringBuilder(super.getProxyUrl(layer));
        proxyUrl.append("&x={x}");
        proxyUrl.append("&y={y}");
        proxyUrl.append("&z={z}");
        return proxyUrl.toString();
    }

}

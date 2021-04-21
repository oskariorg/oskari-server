package fi.nls.oskari.map.layer.formatters;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.WFSConversionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.permissions.model.PermissionType;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_LAYER_COVERAGE;

/**
 * Analysis layer to Oskari layer JSON
 */
public class LayerJSONFormatterANALYSIS extends LayerJSONFormatterUSERDATA {
    private static final String ANALYSIS_BASELAYER_ID = PropertyUtil.get("analysis.baselayer.id");

    private static final String JSKEY_LAYERID = "layerId";
    private static final String JSKEY_OPACITY = "opacity";
    private static final String JSKEY_MINSCALE = "minScale";
    private static final String JSKEY_MAXSCALE = "maxScale";
    private static final String JSKEY_METHODPARAMS = "methodParams";
    private static final String JSKEY_NO_DATA = "no_data";
    private static final String JSKEY_METHOD = "method";
    private static final String JSKEY_BBOX = "bbox";
    private static final String JSKEY_BOTTOM = "bottom";
    private static final String JSKEY_TOP = "top";
    private static final String JSKEY_LEFT = "left";
    private static final String JSKEY_RIGHT = "right";
    private static final String JSKEY_WPSURL = "wpsUrl";
    private static final String JSKEY_WPSNAME = "wpsName";
    private static final String JSKEY_WPSLAYERID = "wpsLayerId";

    private static final String ANALYSIS_RENDERING_ELEMENT = PropertyUtil.get("analysis.rendering.element");

    public JSONObject getJSON(OskariLayer baseLayer, Analysis analysis, String srs, String lang) {
        final JSONObject layerJson = super.getJSON(baseLayer, analysis, srs, lang);
        JSONObject analyseJson = JSONHelper.createJSONObject(analysis.getAnalyse_json());
        String analyseJsonId = JSONHelper.optString(analyseJson, JSKEY_LAYERID);
        if (!analyseJsonId.isEmpty()){
            String id;
            if (analyseJsonId.startsWith(Analysis.ID_PREFIX)) {
                id = Analysis.ID_PREFIX + "_" + ANALYSIS_BASELAYER_ID + "_" + analysis.getId();
            } else {
                id = Analysis.ID_PREFIX + "_" + analyseJsonId + "_" + analysis.getId();
            }
            JSONHelper.putValue(layerJson, KEY_ID, id);
        }

        JSONHelper.putValue(layerJson, JSKEY_WPSURL, AnalysisHelper.getAnalysisRenderingUrl());
        JSONHelper.putValue(layerJson, JSKEY_WPSNAME, ANALYSIS_RENDERING_ELEMENT);
        JSONHelper.putValue(layerJson, JSKEY_WPSLAYERID, analysis.getId());

        int opacity = layerJson.optInt(JSKEY_OPACITY, -1);
        if (opacity > -1) {
            JSONHelper.putValue(layerJson, JSKEY_OPACITY, opacity);
        }
        if (layerJson.has(JSKEY_MINSCALE)) {
            JSONHelper.putValue(layerJson, JSKEY_MINSCALE, layerJson.optInt(JSKEY_MINSCALE, -1));
        }
        if (layerJson.has(JSKEY_MINSCALE)) {
            JSONHelper.putValue(layerJson, JSKEY_MAXSCALE,  layerJson.optInt(JSKEY_MAXSCALE, -1));
        }
        JSONObject bbox = JSONHelper.getJSONObject(analyseJson, JSKEY_BBOX);
        if (bbox != null) {
            try {
                // TODO: ReverencedEnvelope or WKT -> WKTHelper.transform(native, srs)
                Double bottom = bbox.getDouble(JSKEY_BOTTOM);
                Double top = bbox.getDouble(JSKEY_TOP);
                Double left = bbox.getDouble(JSKEY_LEFT);
                Double right = bbox.getDouble(JSKEY_RIGHT);
                String geom = "POLYGON ((" + left + " " + bottom + ", " + right + " " + bottom + ", " +
                        right + " " + top + ", " + left + " " + top + ", " + left + " " + bottom + "))";
                layerJson.put(KEY_LAYER_COVERAGE, geom);
            } catch (JSONException ignored) {
                // Don't add geom if some value is missing or invalid
            }
        }
        parseAttributes(layerJson, analysis, analyseJson, lang);
        return layerJson;
    }
    private static JSONObject parseAttributes (JSONObject layerJson, Analysis analysis, JSONObject analysisJSON, String lang) {
        JSONObject attributes = JSONHelper.getJSONObject(layerJson, "attributes");
        JSONObject data = new JSONObject();
        JSONObject params = JSONHelper.getJSONObject(analysisJSON, JSKEY_METHODPARAMS);
        if (params != null) {
            Object noData = JSONHelper.get(params, JSKEY_NO_DATA);
            if (noData != null) {
                JSONHelper.putValue(data, WFSLayerAttributes.KEY_NO_DATA_VALUE, noData);
            }
        }
        String method = JSONHelper.optString(analysisJSON, JSKEY_METHOD);
        JSONHelper.putValue(attributes, JSKEY_METHOD, method);

        JSONArray filter = new JSONArray();
        JSONObject locale = new JSONObject();
        JSONObject types = new JSONObject();
        for (int j = 1; j < 11; j++) {
            String colx = analysis.getColx(j);
            if (colx != null && colx.contains("=")) {
                String[] splitted = colx.split("=");
                String field = splitted[0];
                String name = splitted[1];
                filter.put(field);
                JSONHelper.putValue(locale, field, name);
                String type = field.startsWith("n") ? WFSConversionHelper.NUMBER : WFSConversionHelper.STRING;
                JSONHelper.putValue(types, field, type);
            }
        }

        JSONHelper.put(data, "filter", filter);
        JSONHelper.putValue(data, "types", types);
        JSONHelper.putValue(data, "locale", JSONHelper.createJSONObject(lang, locale));
        JSONHelper.putValue(attributes, "data", data);
        return attributes;
    }
    // The permissions for analysis layers are inherited from the source layer
    public static void setPermissions(JSONObject layerJson, boolean hasPublish, boolean hasDownload) {
        JSONObject permissions = new JSONObject();
        if (hasPublish) {
            JSONHelper.putValue(permissions, PermissionType.PUBLISH.getJsonKey(), OskariLayerWorker.PUBLICATION_PERMISSION_OK);
        }
        if (hasDownload) {
            JSONHelper.putValue(permissions, PermissionType.DOWNLOAD.getJsonKey(), OskariLayerWorker.DOWNLOAD_PERMISSION_OK);
        }
        JSONHelper.putValue(layerJson, KEY_PERMISSIONS, permissions);
    }

}

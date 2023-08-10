package fi.nls.oskari.map.layer.formatters;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.UserDataLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.wfs.WFSLayerAttributes;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.WFSConversionHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.permissions.model.PermissionType;

/**
 * Analysis layer to Oskari layer JSON
 */
public class LayerJSONFormatterANALYSIS extends LayerJSONFormatterUSERDATA {
    private static final String ANALYSIS_BASELAYER_ID = PropertyUtil.get("analysis.baselayer.id");

    private static final String JSKEY_LAYERID = "layerId";
    private static final String JSKEY_LAYER_TYPE = "layerType";
    private static final String JSKEY_METHODPARAMS = "methodParams";
    private static final String JSKEY_NO_DATA = "no_data";
    private static final String JSKEY_METHOD = "method";
    private static final String JSKEY_BBOX = "bbox";
    private static final String JSKEY_BOTTOM = "bottom";
    private static final String JSKEY_TOP = "top";
    private static final String JSKEY_LEFT = "left";
    private static final String JSKEY_RIGHT = "right";
    private static final String JSKEY_WPS_TYPE = "wpsInputType";

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
        return layerJson;
    }
    @Override
    protected String getGeometryType() {
        return WFSConversionHelper.TYPE_COLLECTION;
    }
    @Override
    protected JSONArray getProperties (UserDataLayer layer, WFSLayerAttributes wfsAttr, String lang) {
        Analysis analysis = (Analysis) layer;
        JSONArray props = new JSONArray();
        // Analysis json contains fields and fieldTypes, maybe should use them instead of 't' or 'n' prefixed names
        // Requires changes in frontend so use columns for now
        for (int i = 1; i < 11; i++) {
            String col = analysis.getColx(i);
            if (col != null && col.contains("=")) {
                String[] splitted = col.split("=");
                JSONObject prop = JSONHelper.createJSONObject("name", splitted[0]); // t${i} || n${i}
                JSONHelper.putValue(prop, "label", splitted[1]);
                String type = splitted[0].startsWith("n") ? WFSConversionHelper.NUMBER : WFSConversionHelper.STRING;
                JSONHelper.putValue(prop, "type", type);
                props.put(prop);
            }
        }
        return props;
    }
    @Override
    protected String getCoverage (UserDataLayer layer, String srs) {
        Analysis analysis = (Analysis) layer;
        JSONObject analyseJson = JSONHelper.createJSONObject(analysis.getAnalyse_json());
        JSONObject bbox = JSONHelper.getJSONObject(analyseJson, JSKEY_BBOX);
        if (bbox == null) {
            return null;
        }
        try {
            Double bottom = bbox.getDouble(JSKEY_BOTTOM);
            Double top = bbox.getDouble(JSKEY_TOP);
            Double left = bbox.getDouble(JSKEY_LEFT);
            Double right = bbox.getDouble(JSKEY_RIGHT);
            String geomWKT = WKTHelper.getBBOX(left,bottom, right, top);
            String nativeSrs = PropertyUtil.get("oskari.native.srs", "EPSG:4326");
            if (nativeSrs.equals(srs)) {
                return geomWKT;
            }
            return WKTHelper.transform(geomWKT, nativeSrs, srs);
        } catch (Exception ignored) {
            // Don't add geom if some value is missing or invalid
        }
        return null;
    }
    @Override
    protected JSONObject getControlData (UserDataLayer layer, WFSLayerOptions wfsOpts) {
        JSONObject controlData = super.getControlData(layer, wfsOpts);
        Analysis analysis = (Analysis) layer;
        JSONObject analyseJson = JSONHelper.createJSONObject(analysis.getAnalyse_json());
        JSONObject params = JSONHelper.getJSONObject(analyseJson, JSKEY_METHODPARAMS);
        if (params != null) {
            JSONHelper.putValue(controlData, WFSLayerAttributes.KEY_NO_DATA_VALUE, JSONHelper.get(params, JSKEY_NO_DATA));
        }
        JSONHelper.putValue(controlData, JSKEY_METHOD, JSONHelper.optString(analyseJson, JSKEY_METHOD));
        // JSONHelper.putValue(controlData, JSKEY_WPS_TYPE, attrData.optString(JSKEY_WPS_TYPE, null));
        JSONHelper.putValue(controlData, "analysisId", analysis.getId());
        JSONHelper.putValue(controlData, "baseLayerId", JSONHelper.optString(analyseJson, JSKEY_LAYERID));
        JSONHelper.putValue(controlData, "baseLayerType", JSONHelper.optString(analyseJson, JSKEY_LAYER_TYPE));
        return controlData;
    }

    private static JSONObject parseAttributes (Analysis analysis, JSONObject analysisJSON, String lang) {
        JSONObject attributes = new JSONObject();
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

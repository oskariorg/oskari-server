package fi.nls.oskari.analysis;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Provides utility methods for analysis
 * Moved JSON generation out of fi.nls.oskari.map.analysis.service.AnalysisDataService.
 */
public class AnalysisHelper {

    private static final String ANALYSIS_LAYERTYPE = "analysislayer";
    private static final String JSKEY_WPSLAYERID = "wpsLayerId";
    private static final String JSKEY_LAYERID = "layerId";

    private static final String JSKEY_NAME = "name";
    private static final String JSKEY_TYPE = "type";
    private static final String JSKEY_OPACITY = "opacity";
    private static final String JSKEY_MINSCALE = "minScale";
    private static final String JSKEY_MAXSCALE = "maxScale";
    private static final String JSKEY_FIELDS = "fields";
    private static final String JSKEY_LOCALES = "locales";

    private static final String JSKEY_BBOX = "bbox";
    private static final String JSKEY_GEOM = "geom";
    private static final String JSKEY_BOTTOM = "bottom";
    private static final String JSKEY_TOP = "top";
    private static final String JSKEY_LEFT = "left";
    private static final String JSKEY_RIGHT = "right";

    private static final String JSKEY_ID = "id";
    private static final String JSKEY_SUBTITLE = "subtitle";
    private static final String JSKEY_ORGNAME = "orgname";
    private static final String JSKEY_INSPIRE = "inspire";
    private static final String JSKEY_WPSURL = "wpsUrl";
    private static final String JSKEY_WPSNAME = "wpsName";
    private static final String JSKEY_RESULT = "result";

    private static final String LAYER_PREFIX = "analysis_";

    private static final String ANALYSIS_ORGNAME = ""; // managed in front
    private static final String ANALYSIS_INSPIRE = ""; // managed in front

    private static final String ANALYSIS_BASELAYER_ID = PropertyUtil.get("analysis.baselayer.id");
    private static final String PROPERTY_RENDERING_URL = PropertyUtil.getOptional("analysis.rendering.url");
    private static final String ANALYSIS_RENDERING_URL = getAnalysisTileUrl();
    private static final String ANALYSIS_RENDERING_ELEMENT = PropertyUtil.get("analysis.rendering.element");

    private static final Logger log = LogFactory.getLogger(AnalysisHelper.class);
    /**
     * Assumes layerId in format analysis[_ignoredParts*]_[analysisId].
     * Parses analysisId or returns -1 if not able to parse it
     * @param layerId
     * @return
     */
    public static long getAnalysisIdFromLayerId(final String layerId) {
        if(!layerId.startsWith(LAYER_PREFIX)) {
            return -1;
        }
        final String[] layerIdSplitted = layerId.split("_");
        if(layerIdSplitted.length < 2) {
            return -1;
        }
        final String analysisId = layerIdSplitted[layerIdSplitted.length - 1];
        return ConversionHelper.getLong(analysisId, -1);
    }

    /**
     * Generate a layer object for frontend.
     * @param al analyse object
     * @return analysis layer data for front mapservice
     * @throws org.json.JSONException
     */
    public static JSONObject getlayerJSON(final Analysis al) {
        return getlayerJSON(al, PropertyUtil.getDefaultLanguage(), false, null, false);
    }

    public static JSONObject getlayerJSON(final Analysis al,
                                          final String lang, final boolean useDirectURL,
                                          final String uuid, final boolean modifyURLs) {
        // TODO: make use of params, see MyPlacesServiceIbatisImpl.getCategoryAsWmsLayerJSON for example
        // TODO: create layer JSON with LayerJSONFormatter
        JSONObject json = new JSONObject();
        // Add correct analyse layer_id to json
        try {
            final JSONObject analyse_js = JSONHelper.createJSONObject(al
                    .getAnalyse_json());
            Long wpsid = al.getId();
            String newid = "-1";
            if (analyse_js.has(JSKEY_LAYERID)) {
                if (analyse_js.getString(JSKEY_LAYERID).indexOf(LAYER_PREFIX) == 0)
                // analyse in Analysislayer (prefix + base analysis wfs layer id
                // +
                // analysis_id)
                {
                    newid = LAYER_PREFIX + ANALYSIS_BASELAYER_ID + "_"
                            + String.valueOf(wpsid);
                } else {
                    // analyse in wfs layer (prefix + wfs layer id +
                    // analysis_id)
                    newid = LAYER_PREFIX + analyse_js.getString(JSKEY_LAYERID)
                            + "_" + String.valueOf(wpsid);
                }

            }

            json.put(JSKEY_ID, newid);
            json.put(JSKEY_TYPE, ANALYSIS_LAYERTYPE);

            json.put(JSKEY_NAME, JSONHelper.getStringFromJSON(analyse_js,
                    JSKEY_NAME, "n/a"));
            json.put(JSKEY_SUBTITLE, "");
            json.put(JSKEY_ORGNAME, ANALYSIS_ORGNAME);
            json.put(JSKEY_INSPIRE, ANALYSIS_INSPIRE);

            json.put(JSKEY_OPACITY, ConversionHelper.getInt(JSONHelper
                    .getStringFromJSON(analyse_js, JSKEY_OPACITY, "80"), 80));
            json.put(JSKEY_MINSCALE, ConversionHelper.getDouble(JSONHelper
                    .getStringFromJSON(analyse_js, JSKEY_MINSCALE, "15000000"),
                    15000000));
            json.put(JSKEY_MAXSCALE, ConversionHelper.getDouble(JSONHelper
                    .getStringFromJSON(analyse_js, JSKEY_MAXSCALE, "1"), 1));
            json.put(JSKEY_FIELDS, getAnalyseNativeFields(al));
            json.put(JSKEY_LOCALES, getAnalyseFields(al));
            json.put(JSKEY_WPSURL, ANALYSIS_RENDERING_URL);
            json.put(JSKEY_WPSNAME, ANALYSIS_RENDERING_ELEMENT);
            json.put(JSKEY_WPSLAYERID, wpsid);
            json.put(JSKEY_RESULT, "");

            if (analyse_js.has(JSKEY_BBOX)) {
                JSONObject bbox = JSONHelper.getJSONObject(analyse_js, JSKEY_BBOX);
                try {
                    String bottom = Double.toString(bbox.getDouble(JSKEY_BOTTOM));
                    String top = Double.toString(bbox.getDouble(JSKEY_TOP));
                    String left = Double.toString(bbox.getDouble(JSKEY_LEFT));
                    String right = Double.toString(bbox.getDouble(JSKEY_RIGHT));
                    String geom = "POLYGON (("+left+" "+bottom+", "+right+" "+bottom+", "+
                            right+" "+top+", "+left+" "+top+", "+left+" "+bottom+"))";
                    json.put(JSKEY_GEOM, geom);
                } catch (Exception ex) {
                    log.debug("Unable to get analysis layer bbox", ex);
                }
            }
        } catch (Exception ex) {
            log.debug("Unable to get analysis layer json", ex);
        }

        return json;
    }

    private static JSONArray getAnalyseFields(Analysis analysis) {
        JSONArray fm = new JSONArray();
        try {
            if (analysis != null) {
                // Fixed 1st is ID
                fm.put("ID");
                for (int j = 1; j < 11; j++) {
                    String colx = analysis.getColx(j);
                    if (colx != null && !colx.isEmpty()) {
                        if (colx.indexOf("=") != -1) {
                            fm.put(colx.split("=")[1]);
                        }
                    }

                }
                // Add geometry for filter and for highlight
                fm.put(AnalysisDataService.ANALYSIS_GEOMETRY_FIELD);
                fm.put("x");
                fm.put("y");
            }
        } catch (Exception ex) {
            log.debug("Unable to get analysis field layer json", ex);
        }
        return fm;
    }

    /**
     * Get native field names
     *
     * @param analysis
     * @return
     */
    private  static JSONArray getAnalyseNativeFields(Analysis analysis) {
        JSONArray fm = new JSONArray();
        try {
            if (analysis != null) {
                // Fixed 1st is ID
                fm.put("__fid");
                for (int j = 1; j < 11; j++) {
                    String colx = analysis.getColx(j);
                    if (colx != null && !colx.isEmpty()) {
                        if (colx.indexOf("=") != -1) {
                            fm.put(colx.split("=")[0]);
                        }
                    }

                }
                // Add geometry for filter and for highlight.
                // ...or maybe not since Transport will add it anyway.
                // This prevents the geometry text from polluting the GFI and feature data.
                //fm.put(AnalysisDataService.ANALYSIS_GEOMETRY_FIELD);
                fm.put("__centerX");
                fm.put("__centerY");
            }
        } catch (Exception ex) {
            log.debug("Unable to get analysis field layer json", ex);
        }
        return fm;
    }

    private static String getAnalysisTileUrl() {
        if (PROPERTY_RENDERING_URL == null) {
            // action_route name points to fi.nls.oskari.control.layer.AnalysisTileHandler
            return PropertyUtil.get("oskari.ajax.url.prefix") + "action_route=AnalysisTile&wpsLayerId=";
        }
        return PROPERTY_RENDERING_URL + "&wpsLayerId=";
    }
}

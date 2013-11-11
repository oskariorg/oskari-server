package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.domain.AnalysisMethodParams;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnalysisDataService {
    private static final String ANALYSIS_INPUT_TYPE_GS_VECTOR = "gs_vector";
    private static final String ANALYSIS_LAYERTYPE = "analysislayer";
    private static final String JSKEY_ANALYSISLAYERS = "analysislayers";
    private static final String JSKEY_WPSLAYERID = "wpsLayerId";
    private static final String JSKEY_LAYERID = "layerId";

    private static final String JSKEY_NAME = "name";
    private static final String JSKEY_TYPE = "type";
    private static final String JSKEY_OPACITY = "opacity";
    private static final String JSKEY_MINSCALE = "minScale";
    private static final String JSKEY_MAXSCALE = "maxScale";
    private static final String JSKEY_FIELDS = "fields";
    private static final String JSKEY_LOCALES = "locales";

    private static final String JSKEY_ID = "id";
    private static final String JSKEY_SUBTITLE = "subtitle";
    private static final String JSKEY_ORGNAME = "orgname";
    private static final String JSKEY_INSPIRE = "inspire";
    private static final String JSKEY_WPSURL = "wpsUrl";
    private static final String JSKEY_WPSNAME = "wpsName";
    private static final String JSKEY_RESULT = "result";

    private static final String LAYER_PREFIX = "analysis_";
    private static final String ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
    private static final String ANALYSIS_RENDERING_URL = "analysis.rendering.url";
    private static final String ANALYSIS_RENDERING_ELEMENT = "analysis.rendering.element";
    private static final String ANALYSIS_ORGNAME = ""; // managed in front
    private static final String ANALYSIS_INSPIRE = ""; // managed in front

    private static final String ANALYSIS_GEOMETRY_FIELD = "geometry";

    private static final String NUMERIC_FIELD_TYPE = "numeric";
    private static final String STRING_FIELD_TYPE = "string";

    private static final Logger log = LogFactory
            .getLogger(AnalysisDataService.class);

    private static final AnalysisStyleDbService styleService = new AnalysisStyleDbServiceIbatisImpl();
    private static final AnalysisDbService analysisService = new AnalysisDbServiceIbatisImpl();
    private static final TransformationService transformationService = new TransformationService();

    final String analysisBaseLayerId = PropertyUtil.get(ANALYSIS_BASELAYER_ID);
    final String analysisRenderingUrl = PropertyUtil.get(ANALYSIS_RENDERING_URL);
    final String analysisRenderingElement = PropertyUtil.get(ANALYSIS_RENDERING_ELEMENT);

    public Analysis storeAnalysisData(final String featureset,
            AnalysisLayer analysislayer, String json, User user) {

        final String wfsURL = PropertyUtil.get("geoserver.wfs.url");
        final String wpsUser = PropertyUtil.get("geoserver.wms.user");
        final String wpsUserPass = PropertyUtil.get("geoserver.wms.pass");

        final AnalysisStyle style = new AnalysisStyle();
        final Analysis analysis = new Analysis();

        try {
            // Insert style row
            final JSONObject stylejs = JSONHelper
                    .createJSONObject(analysislayer.getStyle());
            style.populateFromJSON(stylejs);
        } catch (JSONException e) {
            log.debug("Unable to get AnalysisLayer style JSON", e);
        }
        // FIXME: do we really want to insert possibly empty style??
        log.debug("Adding style", style);
        styleService.insertAnalysisStyleRow(style);

        try {
            // Insert analysis row
            // --------------------
            analysis.setAnalyse_json(json.toString());
            analysis.setLayer_id(analysislayer.getId());
            analysis.setName(analysislayer.getName());
            analysis.setStyle_id(style.getId());
            analysis.setUuid(user.getUuid());
            log.debug("Adding analysis row", analysis);
            analysisService.insertAnalysisRow(analysis);

            // Add analysis_data rows via WFS-T
            // ----------------------------------

            // Convert featureset (wps) to wfs-t
            // ----------------------------------
            final AnalysisMethodParams params = analysislayer
                    .getAnalysisMethodParams();
            final String geometryProperty = transformationService.stripNamespace(params.getGeom());
            // FIXME: wpsToWfst populates fields list AND returns the wfst
            // payload
            // this should be refactored so it returns an object with the fields
            // list and the payload
            // and remove the fields parameter from call
            List<String> fields = new ArrayList<String>();
            final String wfst = transformationService.wpsFeatureCollectionToWfst(featureset, analysis.getUuid(),
                    analysis.getId(), fields, analysislayer.getFieldtypeMap(), geometryProperty);
            log.debug("Produced WFS-T:\n" + wfst);

            final String response = IOHelper.httpRequestAction(wfsURL, wfst,
                    wpsUser, wpsUserPass, null, null, "application/xml");
            log.debug("Posted WFS-T, got", response);

            // If exceptions, return null
            // Check, if exception result set
            if (response.indexOf("ows:Exception") > -1) return null;

            // Check, if any inserted data
            if (response.indexOf("totalInserted>0") > -1) return null;


            // Update col mapping and WPS layer Id into analysis table
            // ---------------------------------------
            // if analysis in analysis - fix field names to original
            if (analysislayer.getInputType().equals(
                    ANALYSIS_INPUT_TYPE_GS_VECTOR)) {
                if (analysislayer.getInputAnalysisId() != null) {
                    fields = this.SwapAnalysisInAnalysisFields(fields,
                            analysislayer.getInputAnalysisId());
                }
            }
            analysis.setCols(fields);

            log.debug("Update analysis row", analysis);
            int updrows = analysisService.updateAnalysisCols(analysis);
            log.debug("Updated rows", updrows);

        } catch (Exception e) {
            log
                    .debug(
                            "Unable to transform WPS to WFS-T or to store analysis data",
                            e);
            return null;
        }

        return analysis;
    }

    /**
     * Merge analyses to one new analyse
     * @param analysislayer data for new analyse
     * @param json params of executed analyse
     * @param user
     * @return Analysis (stored analysis)
     */

    public Analysis mergeAnalysisData(AnalysisLayer analysislayer, String json, User user) {

        final AnalysisStyle style = new AnalysisStyle();
        Analysis analysis = null;

        try {
            // Insert style row
            final JSONObject stylejs = JSONHelper
                    .createJSONObject(analysislayer.getStyle());
            style.populateFromJSON(stylejs);
        } catch (JSONException e) {
            log.debug("Unable to get AnalysisLayer style JSON", e);
        }
        // FIXME: do we really want to insert possibly empty style??
        log.debug("Adding style", style);
        styleService.insertAnalysisStyleRow(style);
        // Get analysis Ids for to merge
        List<Long> ids = analysislayer.getMergeAnalysisIds();
        // at least two layers must be  for merge
        if(ids.size() < 2) return null;

        try {
            // Insert analysis row - use old for seed
            analysis = analysisService.getAnalysisById(ids.get(0));
            // --------------------
            analysis.setAnalyse_json(json.toString());
            analysis.setLayer_id(analysislayer.getId());
            analysis.setName(analysislayer.getName());
            analysis.setStyle_id(style.getId());
            analysis.setUuid(user.getUuid());
            analysis.setOld_id(ids.get(0));
            log.debug("Adding analysis row", analysis);
            analysisService.insertAnalysisRow(analysis);

            // Merge analysis_data
            // ----------------------------------
            log.debug("Merge analysis_data rows", analysis);
            analysisService.mergeAnalysis(analysis, ids);


        } catch (Exception e) {
            log.debug("Unable to join and merge analysis data", e);
            return null;
        }

        return analysis;
    }
    /**
     * Get analysis columns to Map
     * 
     * @param analysis_id
     *            Key to one analysis
     * @return analysis columns
     */
    public Map<String, String> getAnalysisColumns(final String analysis_id) {
        if (analysis_id != null) {
            final Map<String, String> columnNames = new ConcurrentHashMap<String, String>(); // key,
            // name
            Analysis analysis = analysisService
                    .getAnalysisById(ConversionHelper.getLong(analysis_id, 0));
            if (analysis != null) {
                for (int j = 1; j < 11; j++) {
                    String colx = analysis.getColx(j);
                    if (colx != null && !colx.isEmpty()) {
                        if (colx.indexOf("=") != -1) {
                            columnNames.put(colx.split("=")[0],
                                    colx.split("=")[1]);
                        }
                    }

                }
                return columnNames;
            }
        }
        return null;
    }
    
    /**
     * Get analysis columns to Json string
     * 
     * @param analysis_id
     *            Key to one analysis
     * @return analysis columns
     */
    public String getAnalysisNativeColumns(final String analysis_id) {
        if (analysis_id != null) {
            final List<String> columnNames = new ArrayList<String>(); // key,
            // name
            Analysis analysis = analysisService
                    .getAnalysisById(ConversionHelper.getLong(analysis_id, 0));
            if (analysis != null) {
                // fixed extra becauseof WFS
               // columnNames.add("__fid");
                for (int j = 1; j < 11; j++) {
                    String colx = analysis.getColx(j);
                    if (colx != null && !colx.isEmpty()) {
                        if (colx.indexOf("=") != -1) {
                            columnNames.add(colx.split("=")[0]);
                           
                        }
                    }

                }
                // Add geometry for filter and for highlight
                columnNames.add(ANALYSIS_GEOMETRY_FIELD);
                return "{default:"+columnNames.toString()+"}";
            }
        }
        return null;
    }
    /**
     * Get analysis column types to Map
     *
     * @param analysis_id
     *            Key to one analysis
     * @return analysis columns and types
     */
    public JSONObject getAnalysisNativeColumnTypes(final String analysis_id) {
        JSONObject columnTypes = new JSONObject(); // {fieldName1:type,fieldname2:type ... (type is string or numeric)
        if (analysis_id == null) return columnTypes;
        // name
        try {
            Analysis analysis = analysisService
                    .getAnalysisById(ConversionHelper.getLong(analysis_id, 0));
            if (analysis != null) {
                for (int j = 1; j < 11; j++) {
                    String colx = analysis.getColx(j);
                    if (colx != null && !colx.isEmpty()) {
                        if (colx.indexOf("=") != -1) {
                            String field = colx.split("=")[0];
                            String wfstype = STRING_FIELD_TYPE;
                            if (field.substring(0, 1).equals("n")) wfstype = NUMERIC_FIELD_TYPE;
                            //TODO: add "date" type management  (Date, dateTime)

                            columnTypes.put(field, wfstype);
                        }
                    }

                }
                return columnTypes;
            }
        } catch (Exception ee) {
            log.debug("Unable to get analysis field types", ee);
        }
        return columnTypes;
    }

    /**
     * @param fieldsin
     *            raw field names mapping
     * @param analysis_id
     *            analysis_id of input analysis
     * @return List of field names mapping
     */
    public List<String> SwapAnalysisInAnalysisFields(List<String> fieldsin,
            String analysis_id) {

        Map<String, String> colnames = this.getAnalysisColumns(analysis_id);

        for (int i = 0; i < fieldsin.size(); i++) {
            String col = fieldsin.get(i);
            if (!col.isEmpty()) {
                if (col.indexOf("=") != -1) {
                    String[] cola = col.split("=");
                    if (colnames.containsKey(cola[1])) {
                        fieldsin.set(i, cola[0] + "=" + colnames.get(cola[1]));
                    }

                }
            }

        }
        return fieldsin;
    }

    /**
     * Switch field name to analysis field name
     * (nop, if field name already analysis field name)
     * @param field_in
     *            original field name
     * @param analysis_id
     *            analysis_id of input analysis
     * @return field name in analysis (eg.t1)
     */
    public String SwitchField2AnalysisField(String field_in, String analysis_id) {

        Map<String, String> colnames = this.getAnalysisColumns(analysis_id);

        for (Map.Entry<String, String> entry : colnames.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().equals(field_in)) {
                return key;
            }

        }

        return field_in;
    }
    /**
     * Switch field name to original field name
     * (nop, if field name already analysis field name)
     * @param field_in
     *            analysis field name (eg. t1)
     * @param analysis_id
     *            analysis_id of input analysis
     * @return field name in original wfs layer (eg. rakennustunnus)
     */
    public String SwitchField2OriginalField(String field_in, String analysis_id) {

        Map<String, String> colnames = this.getAnalysisColumns(analysis_id);

        for (Map.Entry<String, String> entry : colnames.entrySet()) {
            String key = entry.getKey();
            if (key.equals(field_in)) {
                return entry.getValue();
            }

        }

        return field_in;
    }
    /**
     * @param uid
     *            User uuid
     * @param lang
     *            language
     * @return Analysis layers of one user retreaved by uuid
     * @throws ServiceException
     */
    public JSONObject getListOfAllAnalysisLayers(String uid, String lang)
            throws ServiceException {

        final JSONObject listLayer = new JSONObject();
        try {
            List<Analysis> layers = analysisService.getAnalysisByUid(uid);
            final JSONArray layersJSON = new JSONArray();
            listLayer.put(JSKEY_ANALYSISLAYERS, layersJSON);
            for (Analysis al : layers) {

                // Parse analyse layer json out analysis
                JSONObject analyselayer = getlayerJSON(al);
                listLayer.accumulate(JSKEY_ANALYSISLAYERS, analyselayer);
            }
        } catch (Exception ex) {
            throw new ServiceException("Unable to get analysis layers", ex);
        }
        return listLayer;
    }

    // Analyse json sample
    // {"name":"Analyysi_Tampereen ","method":"buffer","fields":["__fid","metaDataProperty","description","name","boundedBy","location","NIMI","GEOLOC","__centerX","__centerY"],"layerId":264,"layerType":"wfs","methodParams":{"distance":"22"},"opacity":100,"style":{"dot":{"size":"4","color":"CC9900"},"line":{"size":"2","color":"CC9900"},"area":{"size":"2","lineColor":"CC9900","fillColor":"FFDC00"}},"bbox":{"left":325158,"bottom":6819828,"right":326868,"top":6820378}}
    /**
     * @param al
     *            analyse object
     * @return analysis layer data for front mapservice
     * @throws JSONException
     */
    public JSONObject getlayerJSON(Analysis al)

    throws JSONException {
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
                    newid = LAYER_PREFIX + analysisBaseLayerId + "_"
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
                    .getStringFromJSON(analyse_js, JSKEY_MINSCALE, "1500000"),
                    1500000));
            json.put(JSKEY_MAXSCALE, ConversionHelper.getDouble(JSONHelper
                    .getStringFromJSON(analyse_js, JSKEY_MAXSCALE, "1"), 1));
            json.put(JSKEY_FIELDS, this.getAnalyseNativeFields(al));
            json.put(JSKEY_LOCALES, this.getAnalyseFields(al));
            json.put(JSKEY_WPSURL, analysisRenderingUrl);
            json.put(JSKEY_WPSNAME, analysisRenderingElement);
            json.put(JSKEY_WPSLAYERID, wpsid);
            json.put(JSKEY_RESULT, "");
        } catch (Exception ex) {
            log.debug("Unable to get analysis layer json", ex);
        }

        return json;
    }

    public JSONArray getAnalyseFields(Analysis analysis) {
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
                fm.put(ANALYSIS_GEOMETRY_FIELD);
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
     * @param analysis
     * @return
     */
    public JSONArray getAnalyseNativeFields(Analysis analysis) {
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
                // Add geometry for filter and for highlight
                fm.put(ANALYSIS_GEOMETRY_FIELD);
                fm.put("__centerX");
                fm.put("__centerY");
            }
        } catch (Exception ex) {
            log.debug("Unable to get analysis field layer json", ex);
        }
        return fm;
    }
}

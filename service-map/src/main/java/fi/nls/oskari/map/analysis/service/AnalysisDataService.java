package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.*;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalysisDataService {
    private static final String ANALYSIS_INPUT_TYPE_GS_VECTOR = "gs_vector";

    public static final String ANALYSIS_GEOMETRY_FIELD = "geometry";

    private static final String NUMERIC_FIELD_TYPE = "numeric";
    private static final String STRING_FIELD_TYPE = "string";

    private static final Logger log = LogFactory
            .getLogger(AnalysisDataService.class);

    private static final AnalysisStyleDbService styleService = new AnalysisStyleDbServiceMybatisImpl();
    private static final AnalysisDbService analysisService = new AnalysisDbServiceMybatisImpl();
    private static final TransformationService transformationService = new TransformationService();

    public Analysis storeAnalysisData(final String featureset,
            AnalysisLayer analysislayer, String json, User user) throws ServiceException {

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
        styleService.insertAnalysisStyleRow(style);

        try {
            // Insert analysis row
            // --------------------
            analysis.setAnalyse_json(json.toString());
            analysis.setLayer_id(analysislayer.getId());
            analysis.setName(analysislayer.getName());
            analysis.setStyle_id(style.getId());
            analysis.setUuid(user.getUuid());
            if (analysislayer.getOverride_sld() != null && !analysislayer.getOverride_sld().isEmpty())
                analysis.setOverride_SLD(analysislayer.getOverride_sld());
            log.debug("Adding analysis row", analysis);
            analysisService.insertAnalysisRow(analysis);

            // Add analysis_data rows via WFS-T
            // ----------------------------------

            // Convert featureset (wps) to wfs-t
            // ----------------------------------
            final AnalysisMethodParams params = analysislayer.getAnalysisMethodParams();
            final String geometryProperty = transformationService.stripNamespace(params.getGeom());
            // FIXME: wpsToWfst populates fields list AND returns the wfst
            // payload
            // this should be refactored so it returns an object with the fields
            // list and the payload
            // and remove the fields parameter from call
            List<String> fields = new ArrayList<String>();
            final String wfst = transformationService.wpsFeatureCollectionToWfst(featureset, analysis.getUuid(),
                    analysis.getId(), fields, analysislayer.getFieldtypeMap(), geometryProperty, params.getResponsePrefix());
            log.debug("Produced WFS-T:\n" + wfst);

            final String response = IOHelper.httpRequestAction(wfsURL, wfst,
                    wpsUser, wpsUserPass, null, null, "application/xml");
            log.debug("Posted WFS-T, got", response);

            // If exceptions, return null
            // Check, if exception result set
            if (response.indexOf("ows:Exception") > -1) return null;

            // Check, if any inserted data
            if (response.indexOf("totalInserted>0") > -1) return null;

            // Set fields and field order, if fields are known before analysis


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
            // if analysis in analysis and second layer is analysislayer - fix field names to original
            fields = this.SwapSecondAnalysisFieldNames(fields, analysislayer);

            // Reorder  columns for difference method
            fields = this.FitFixedFieldOrder4Difference(fields, analysislayer);

            // Localize  Spatial join aggregate fields
            fields = this.LocalizeFields4spAggregate(fields, analysislayer);

            analysis.setCols(fields);

            log.debug("Update analysis row", analysis);
            long updrows = analysisService.updateAnalysisCols(analysis);
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

    public Analysis getAnalysisById(long id) {
        return analysisService.getAnalysisById(id);
    }

    /**
     * Merge analyses to one new analyse
     *
     * @param analysislayer data for new analyse
     * @param json          params of executed analyse
     * @param user
     * @return Analysis (stored analysis)
     */
    public Analysis mergeAnalysisData(AnalysisLayer analysislayer, String json, User user) throws ServiceException {

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
        if (ids.size() < 2) return null;

        try {
            // Insert analysis row - use old for seed
            analysis = getAnalysisById(ids.get(0));
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
     * @param analysis_id Key to one analysis
     * @return analysis columns
     */
    public Map<String, String> getAnalysisColumns(final String analysis_id) {
        long id = ConversionHelper.getLong(analysis_id, -1L);
        if (id == -1L) {
            return null;
        }
        Analysis analysis = analysisService.getAnalysisById(id);
        if (analysis == null) {
            return null;
        }

        // key, name
        Map<String, String> columnNames = new LinkedHashMap<String, String>();
        for (int j = 1; j < 11; j++) {
            String colx = analysis.getColx(j);
            if (colx != null && !colx.isEmpty()) {
                int i = colx.indexOf('=');
                if (i > 0) {
                    String key = colx.substring(0, i);
                    String name = colx.substring(i + 1);
                    columnNames.put(key, name);
                }
            }
        }
        return columnNames;
    }

    /**
     * Get analysis columns to Json string
     *
     * @param analysis_id Key to one analysis (should be number value as string)
     * @return analysis columns
     */
    public String getAnalysisNativeColumns(final String analysis_id) {
        if (analysis_id == null) {
            return null;
        }
        return getAnalysisNativeColumns(ConversionHelper.getLong(analysis_id, 0));
    }

    /**
     * Get analysis columns to Json string
     *
     * @param analysis_id Key to one analysis
     * @return analysis columns
     */
    public String getAnalysisNativeColumns(final long analysis_id) {
        final Analysis analysis = analysisService.getAnalysisById(analysis_id);
        return getAnalysisNativeColumns(analysis);
    }

    /**
     * Get analysis columns to Json string
     *
     * @param analysis analysis to get columns from
     * @return analysis columns in a JSON string or null if parameter is null
     */
    public String getAnalysisNativeColumns(final Analysis analysis) {
        if (analysis == null) {
            return null;
        }
        final List<String> columnNames = new ArrayList<String>();
        for (int j = 1; j < 11; j++) {
            // format of colx is "<internal name f.ex t1>=<actual name in original data>"
            String colx = analysis.getColx(j);
            if (colx == null || colx.isEmpty() || colx.indexOf("=") == -1) {
                // skip if column data is not valid(?)
                continue;
            }
            String columnName = colx.split("=")[0];
            if (columnName.equals(ANALYSIS_GEOMETRY_FIELD)) {
                // ignore geometry field (it gets added to the query by WFS anyway)
                continue;
            }
            columnNames.add(colx.split("=")[0]);
        }
        return "{default:" + columnNames.toString() + "}";
    }

    /**
     * Get analysis column types to Map
     *
     * @param analysis_id Key to one analysis
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
     * @param fieldsin    raw field names mapping
     * @param analysis_id analysis_id of input analysis
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
     * Replace analysislayer internal field names in 2 layers analysis
     * when second layer is analysislayer
     *
     * @param fieldsin raw field names mapping
     * @return List of field names mapping
     */
    public List<String> SwapSecondAnalysisFieldNames(List<String> fieldsin, AnalysisLayer analysisLayer) {


        if (analysisLayer.getAnalysisMethodParams() instanceof IntersectJoinMethodParams) {
            IntersectJoinMethodParams params = (IntersectJoinMethodParams) analysisLayer.getAnalysisMethodParams();
            if (params.getWps_reference_type2().equals(
                    ANALYSIS_INPUT_TYPE_GS_VECTOR)) {
                if (params.getLayer_id2() != null) {
                    Map<String, String> colnames = this.getAnalysisColumns(params.getLayer_id2());
                    // Replace internal analysis field names (t1,...)
                    for (int i = 0; i < fieldsin.size(); i++) {
                        String col = fieldsin.get(i);
                        if (!col.isEmpty()) {
                            if (col.indexOf("=") != -1) {
                                String[] cola = col.split("=");
                                // Remove join body field part
                                cola[1] = cola[1].replace(transformationService.stripNamespace(params.getTypeName2()) + "_", "");
                                if (colnames.containsKey(cola[1])) {
                                    fieldsin.set(i, cola[0] + "=" + transformationService.stripNamespace(params.getTypeName2()) + "_" + colnames.get(cola[1]));
                                }

                            }
                        }

                    }

                }

            }

        } else if (analysisLayer.getAnalysisMethodParams() instanceof SpatialJoinStatisticsMethodParams) {
            SpatialJoinStatisticsMethodParams params = (SpatialJoinStatisticsMethodParams) analysisLayer.getAnalysisMethodParams();
            if (params.getWps_reference_type2().equals(
                    ANALYSIS_INPUT_TYPE_GS_VECTOR)) {
                if (params.getLayer_id2() != null) {
                    Map<String, String> colnames = this.getAnalysisColumns(params.getLayer_id2());
                    // Replace internal analysis field names (t1,...)
                    for (int i = 0; i < fieldsin.size(); i++) {
                        String col = fieldsin.get(i);
                        if (!col.isEmpty() && col.contains("=")) {
                            String[] cola = col.split("=");
                            if (colnames.containsKey(cola[1])) {
                                fieldsin.set(i, cola[0] + "=" + transformationService.stripNamespace(colnames.get(cola[1])));
                            }

                        }

                    }

                }

            }

        }


        return fieldsin;
    }

    /**
     * ReOrder analysis columns - only for difference method
     *
     * @param fieldsin raw field names mapping based on gml featurecollection
     * @return List of field names mapping
     */
    public List<String> FitFixedFieldOrder4Difference(List<String> fieldsin, AnalysisLayer analysisLayer) {


        if (analysisLayer.getAnalysisMethodParams() instanceof DifferenceMethodParams) {
            List<String> fields = new ArrayList<String>();

            // Reorder analysis field names (t1,...)
            for (int k = 0; k < analysisLayer.getFields().size(); k++) {
                String orderedCol = analysisLayer.getFields().get(k);

                for (int i = 0; i < fieldsin.size(); i++) {
                    String col = fieldsin.get(i);
                    if (!col.isEmpty()) {
                        if (col.contains(orderedCol)) {
                            fields.add(col);
                            break;
                        }
                    }

                }
            }
            if (fields.size() == fieldsin.size()) {
                return fields;
            }
        }

        return fieldsin;
    }

    /**
     * Localize  Wps generated  aggregate fields
     * - this is only for spatial join aggregate method
     * @param fieldsin
     * @param analysisLayer
     * @return
     */

    public List<String> LocalizeFields4spAggregate(List<String> fieldsin, AnalysisLayer analysisLayer) {


        if (analysisLayer.getAnalysisMethodParams() instanceof SpatialJoinStatisticsMethodParams) {
            SpatialJoinStatisticsMethodParams params = (SpatialJoinStatisticsMethodParams) analysisLayer.getAnalysisMethodParams();
            // Rename analysis field names (t1,...)
            for (Map.Entry<String, String> entry : params.getLocalemap().entrySet()) {
                String id = entry.getKey();
                String label = entry.getValue();
                for (int i = 0; i < fieldsin.size(); i++) {
                    String col = fieldsin.get(i);
                    if (!col.isEmpty()) {
                        if (col.contains("=")) {
                            String[] cola = col.split("=");
                            if (cola[1].equals(id))
                                fieldsin.set(i, cola[0] + "=" + label);
                        }
                    }

                }
            }

        }

        return fieldsin;
    }
    /**
     * Switch field name to analysis field name
     * (nop, if field name already analysis field name)
     *
     * @param field_in    original field name
     * @param analysis_id analysis_id of input analysis
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
     *
     * @param field_in    analysis field name (eg. t1)
     * @param analysis_id analysis_id of input analysis
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

    // Analyse json sample
    // {"name":"Analyysi_Tampereen ","method":"buffer","fields":["__fid","metaDataProperty","description","name","boundedBy","location","NIMI","GEOLOC","__centerX","__centerY"],"layerId":264,"layerType":"wfs","methodParams":{"distance":"22"},"opacity":100,"style":{"dot":{"size":"4","color":"CC9900"},"line":{"size":"2","color":"CC9900"},"area":{"size":"2","lineColor":"CC9900","fillColor":"FFDC00"}},"bbox":{"left":325158,"bottom":6819828,"right":326868,"top":6820378}}

    public JSONObject getlayerJSON(long id) {
        Analysis analysis = analysisService.getAnalysisById(id);
        return AnalysisHelper.getlayerJSON(analysis);
    }


}

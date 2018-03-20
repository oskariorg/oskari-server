package fi.nls.oskari.control.layer;

import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.analysis.service.AnalysisDbServiceMybatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns user's analysis data as JSON.
 * Only for aggregate method
 */
@OskariActionRoute("GetAnalysisData")
public class GetAnalysisDataHandler extends ActionHandler {

    private static final AnalysisDbService analysisService = new AnalysisDbServiceMybatisImpl();

    private static final String ANALYSE_ID = "analyse_id";
    private static final String JSKEY_ANALYSISDATA = "analysisdata";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        final long id = ConversionHelper.getLong(params.getHttpParam(ANALYSE_ID), -1L);
        if (id == -1L) {
            throw new ActionParamsException("Parameter missing or non-numeric: "
                    + ANALYSE_ID + "=" + params.getHttpParam(ANALYSE_ID));
        }

        Analysis analysis = findAnalysis(id);

        String selectItems = AnalysisHelper.getAnalysisSelectItems(analysis);
        if (selectItems == null) {
            throw new ActionException("Unable to retrieve Analysis data, "
                    + "this Analysis is not stored correctly, id:" + id);
        }

        String uid = params.getUser().getUuid();
        List<HashMap<String, Object>> list = findAnalysisData(id, uid, selectItems);

        final JSONArray rows = new JSONArray();
        for (HashMap<String, Object> analysisData : list) {
            final JSONObject row = convertToOldResultJSON(analysisData, selectItems);
            if (row != null) {
                rows.put(row);
            }
        }

        final JSONObject response = new JSONObject();
        JSONHelper.putValue(response, JSKEY_ANALYSISDATA, rows);
        JSONHelper.putValue(response, ANALYSE_ID, id);

        ResponseHelper.writeResponse(params, response);
    }

    private Analysis findAnalysis(long id) throws ActionException {
        Analysis analysis;
        try {
            analysis = analysisService.getAnalysisById(id);
        } catch (Exception e) {
            throw new ActionException("Unexpected error occured trying to find analysis", e);
        }
        if (analysis == null) {
            throw new ActionParamsException("Analysis not found, id: " + id);
        }
        return analysis;
    }

    private List<HashMap<String, Object>> findAnalysisData(long id, String uid, String selectItems)
            throws ActionException {
        List<HashMap<String, Object>> list;
        try {
            list = analysisService.getAnalysisDataByIdUid(id, uid, selectItems);
        } catch (Exception e) {
            throw new ActionException("Unexpected error occured trying to find analysis data", e);
        }
        if (list.isEmpty()) {
            throw new ActionParamsException("Could not find analysis data");
        }
        return list;
    }

    /**
     * Converts aggregate analysis result to old result syntax for multiselect on client side
     * and returns a JSONObject as aggregate results of one property
     *
     * @param analysisData
     * @return JSONObject {property:{aggregate value1, aggregate value2,..}}
     */
    private JSONObject convertToOldResultJSON(HashMap<String, Object> analysisData, String selectColumns) {
        if (analysisData == null) {
            return null;
        }
        final JSONObject row = new JSONObject();
        String columnName = null;
        for (Map.Entry<String, Object> entry : analysisData.entrySet()) {
            final Object value = entry.getValue();
            final String key = entry.getKey();
            if (selectColumns.indexOf("As " + key) != -1) {
                columnName = value.toString();
                continue;
            }
            // try to map as JSON
            JSONHelper.putValue(row, key, value);

        }
        return (columnName != null) ? JSONHelper.createJSONObject(columnName, row) : null;
    }
}

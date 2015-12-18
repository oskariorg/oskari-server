package fi.nls.oskari.control.layer;

import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.analysis.service.AnalysisDbServiceIbatisImpl;
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

    private static final Logger log = LogFactory.getLogger(GetAnalysisDataHandler.class);
    private static final AnalysisDbService analysisService = new AnalysisDbServiceIbatisImpl();

    private static final String ANALYSE_ID = "analyse_id";
    private static final String JSKEY_ANALYSISDATA = "analysisdata";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        // only for logged in users!
        params.requireLoggedInUser();

        final long id = ConversionHelper.getLong(params.getHttpParam(ANALYSE_ID), -1);
        if (id == -1) {
            throw new ActionParamsException("Parameter missing or non-numeric: " + ANALYSE_ID + "=" + params.getHttpParam(ANALYSE_ID));
        }

        final User user = params.getUser();
        // Get analysis select items
        final Analysis analysis = analysisService.getAnalysisById(id);
        final String select_items = AnalysisHelper.getAnalysisSelectItems(analysis);

        final JSONObject response = new JSONObject();
        if (select_items != null) {

            final List<HashMap<String, Object>> list = analysisService.getAnalysisDataByIdUid(id, user.getUuid(), select_items);
            final JSONArray rows = new JSONArray();
            for (HashMap<String, Object> analysisData : list) {
                final JSONObject row = convertToOldResultJSON(analysisData, select_items);
                if (row != null) {
                    rows.put(row);
                }
            }
            JSONHelper.putValue(response, JSKEY_ANALYSISDATA, rows);
            JSONHelper.putValue(response, ANALYSE_ID, id);
        }

        ResponseHelper.writeResponse(params, response);
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

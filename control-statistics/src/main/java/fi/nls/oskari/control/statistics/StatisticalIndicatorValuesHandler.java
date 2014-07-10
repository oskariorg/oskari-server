package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.sotka.requests.SotkaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import static fi.nls.oskari.control.statistics.util.Constants.*;

/**
 * Returns indicators for statistical datasource registered to Oskari.
 */
@OskariActionRoute("StatisticalIndicatorValues")
public class StatisticalIndicatorValuesHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorValuesHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {

        final int datasourceId = params.getRequiredParamInt(PARAM_DATASOURCE);
        final int indicatorId = params.getRequiredParamInt(PARAM_ID);
        final String optionsStr = params.getHttpParam(PARAM_OPTIONS);
        JSONObject options =  JSONHelper.createJSONObject(optionsStr);

        ResponseHelper.writeResponse(params, getDummyValues("" + indicatorId, options));
    }
    private JSONObject getDummyValues(String indicatorId, JSONObject options) throws ActionException {

        final SotkaRequest req = SotkaRequest.getInstance("data");
        req.setGender(options.optString("sex"));
        req.setVersion("1.1");
        req.setIndicator(indicatorId);
        req.setYears(new String[]{options.optString("year")});
        final String data = req.getData();
        return JSONHelper.createJSONObject(data);
    }

}

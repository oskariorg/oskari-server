package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import static fi.nls.oskari.control.statistics.util.Constants.*;
/**
 * Returns indicators for statistical datasource registered to Oskari.
 */
@OskariActionRoute("StatisticalIndicators")
public class StatisticalIndicatorsHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorsHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {

        final int datasource = params.getRequiredParamInt(PARAM_DATASOURCE);

        JSONArray indicators = new JSONArray();
        JSONObject response = JSONHelper.createJSONObject("indicators", indicators);
        // TODO: load indicators based on datasource
        ResponseHelper.writeResponse(params, response);
    }

}

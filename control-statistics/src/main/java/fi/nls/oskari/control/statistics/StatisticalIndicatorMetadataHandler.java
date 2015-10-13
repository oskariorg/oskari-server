package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import static fi.nls.oskari.control.statistics.util.Constants.PARAM_DATASOURCE;
import static fi.nls.oskari.control.statistics.util.Constants.PARAM_ID;

/**
 * Returns metadata for indicator in statistical datasource registered to Oskari.
 */
@OskariActionRoute("StatisticalIndicatorMetadata")
public class StatisticalIndicatorMetadataHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorMetadataHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {

        final int datasourceId = params.getRequiredParamInt(PARAM_DATASOURCE);
        final int indicatorId = params.getRequiredParamInt(PARAM_ID);

        // TODO: load indicators metadata based on datasource/indicator
        ResponseHelper.writeResponse(params, getDummyIndicators(datasourceId, indicatorId));
    }

    private JSONObject getDummyIndicators(int datasourceId, int indicatorId) throws ActionException {

        final SotkaRequest req = SotkaRequest.getInstance("indicator_metadata");
        req.setGender("");
        req.setVersion("1.1");
        req.setIndicator("" +indicatorId);
        //req.setYears(params.getRequest().getParameterValues(PARM_YEARS));
        final String data = req.getData();
        return JSONHelper.createJSONObject(data);
    }
}

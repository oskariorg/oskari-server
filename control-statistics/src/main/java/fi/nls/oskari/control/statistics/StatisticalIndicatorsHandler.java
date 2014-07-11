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
import org.json.JSONArray;

import static fi.nls.oskari.control.statistics.util.Constants.*;
/**
 * Returns indicators for statistical datasource registered to Oskari.
 */
@OskariActionRoute("StatisticalIndicators")
public class StatisticalIndicatorsHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorsHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {

        final int datasource = params.getRequiredParamInt(PARAM_DATASOURCE);

        // TODO: load indicators based on datasource, add abstraction and transforming
        // for result so client always gets the response in same format
        // indicator regions should match statslayers
        // (see StatisticalIndicatorRegionCategories/StatisticalIndicatorRegions action route)
        ResponseHelper.writeResponse(params, getDummyIndicators());
    }

    private JSONArray getDummyIndicators() throws ActionException {

        final SotkaRequest req = SotkaRequest.getInstance("indicators");
        req.setGender("");
        req.setVersion("1.1");
        req.setIndicator("");
        //req.setYears(params.getRequest().getParameterValues(PARM_YEARS));
        final String data = req.getData();
        return JSONHelper.createJSONArray(data);
    }

}

package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.sotka.requests.SotkaRequest;
import fi.nls.oskari.integration.sotka.SotkaRegionParser;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import static fi.nls.oskari.control.statistics.util.Constants.*;

/**
 * Returns indicators for statistical datasource registered to Oskari.
 */
@OskariActionRoute("StatisticalIndicatorValues")
public class StatisticalIndicatorValuesHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorValuesHandler.class);
    private SotkaRegionParser sotkaParser = null;

    public void init() {
        sotkaParser = new SotkaRegionParser();
    }

    public void handleAction(ActionParameters params) throws ActionException {
        // FIXME: now always proxies to SotkaNet
        final int datasourceId = params.getRequiredParamInt(PARAM_DATASOURCE);
        final int indicatorId = params.getRequiredParamInt(PARAM_ID);
        final String optionsStr = params.getHttpParam(PARAM_OPTIONS);
        JSONObject options =  JSONHelper.createJSONObject(optionsStr);

        ResponseHelper.writeResponse(params, getDummyValues("" + indicatorId, options));
    }
    private JSONArray getDummyValues(String indicatorId, JSONObject options) throws ActionException {

        final SotkaRequest req = SotkaRequest.getInstance("data");
        req.setGender(options.optString("sex"));
        req.setVersion("1.0");
        req.setIndicator(indicatorId);
        req.setYears(new String[]{options.optString("year")});
        final String data = req.getData();
        return modifyRegionMap(JSONHelper.createJSONArray(data));
    }

    private JSONArray modifyRegionMap(final JSONArray values) {
        for(int i = 0; i < values.length(); ++i) {
            JSONObject obj = values.optJSONObject(i);
            // override region codes to match geoserver material
            final String code = sotkaParser.getCode(obj.optInt("region"));
            if(code != null) {
                JSONHelper.putValue(obj, "region", code);
            }
        }
        return values;
    }

}

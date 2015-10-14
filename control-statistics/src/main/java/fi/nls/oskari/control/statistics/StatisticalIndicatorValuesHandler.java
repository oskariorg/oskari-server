package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaRegionParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

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

        ResponseHelper.writeResponse(params, getDummyValues(datasourceId, "" + indicatorId, options));
    }
    private JSONArray getDummyValues(int datasourceId, String indicatorId, JSONObject options) throws ActionException {

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
            Map<String, Object> region = sotkaParser.getRegionById(obj.optInt("region"));

            // TODO: region should be prefixed with region category - see StatisticalIndicatorRegionsHandler
            // TODO: categoryId == layerId where given region can be presented
            // for now it's harcoded 1=kunta, 2=seutukunta, 3= sairaanhoitopiiri
            // should map sotka region type to it
            int categoryId = 1;
            final String category = (String)region.get(SotkaRegionParser.CATEGORY_FIELD);
            if("KUNTA".equals(category)) {
                categoryId = 1;
            }
            else if("SEUTUKUNTA".equals(category)) {
                categoryId = 2;
            }
            else if("SAIRAANHOITOPIIRI".equals(category)) {
                categoryId = 3;
            }
            final String code = (String) region.get("code"); //sotkaParser.getCode(obj.optInt("region"));
            if(code != null) {
                JSONHelper.putValue(obj, "region", categoryId + "__" + code);
            }
        }
        return values;
    }

}

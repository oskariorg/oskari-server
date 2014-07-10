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
import org.json.JSONObject;

import static fi.nls.oskari.control.statistics.util.Constants.PARAM_DATASOURCE;

/**
 * Returns all region categories that can be presented visually in Oskari (think all registered statslayers).
 * These layers can be used to visualize indicators.
 */
@OskariActionRoute("StatisticalIndicatorRegionCategories")
public class StatisticalIndicatorRegionCategoriesHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorRegionCategoriesHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {

        // TODO: load statslayers that are in the system -> id == layerId, locale == layer.locale
        JSONArray response = new JSONArray();
        response.put(JSONHelper.createJSONObject("{ \"id\" : 1, \"locale\" : { \"fi\" : \"Kunta\", \"en\" : \"Municipality\"}}"));
        response.put(JSONHelper.createJSONObject("{ \"id\" : 2, \"locale\" : { \"fi\" : \"Seutukunta\", \"en\" : \"Seutukunta\"}}"));

        //statslayers should have properties like:
        // * region_id = kuntakoodi
        // * locale={fi:'Kunta', en:'Municipality'})

        ResponseHelper.writeResponse(params, response);
    }

}

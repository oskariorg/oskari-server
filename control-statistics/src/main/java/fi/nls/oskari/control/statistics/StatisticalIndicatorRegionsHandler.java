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

import static fi.nls.oskari.control.statistics.util.Constants.PARAM_ID;

/**
 * Returns regions (=features) available in given category (=statslayer) that are used to visualize indicators
 */
@OskariActionRoute("StatisticalIndicatorRegions")
public class StatisticalIndicatorRegionsHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorRegionsHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {

        final int categoryId = params.getRequiredParamInt(PARAM_ID);
        // 1) load layer with id <categoryId> (should be of type statslayer)
        // 2) list all features in the layer
        JSONArray response = new JSONArray();
        // these regions(features) should match regions referenced by indicators
        // id == layer.region_id (doesn't exist yet)
        if(categoryId == 1) {
            // "layerId" : something_something, // 'oskari:kunnat2013'
            response.put(JSONHelper.createJSONObject("{ \"id\" : \"091\", \"locale\" : { \"fi\" : \"Helsinki\"}}"));
            response.put(JSONHelper.createJSONObject("{ \"id\" : \"837\", \"locale\" : { \"fi\" : \"Tampere\"}}"));
        }
        else if( categoryId == 2) {
            // "layerId" : something_else, // 'oskari:seutukunta'
            response.put(JSONHelper.createJSONObject("{ \"id\" : \"011\", \"locale\" : { \"fi\" : \"Helsingin seutukunta\"}}"));
            response.put(JSONHelper.createJSONObject("{ \"id\" : \"064\", \"locale\" : { \"fi\" : \"Tampereen seutukunta\"}}"));
        }

/*
From SotkaNet:
{
"id": 833,
"code": "1", <- should be reference to statslayer.region_id
"category": "ALUEHALLINTOVIRASTO", <- should be reference to statslayer.id
"title": { <- should come from feature property in Vector-material that the statslayer presents (feature title)
"fi": "Etelä-Suomen AVIn alue",
"en": "Area for Southern Finland AVI",
"sv": "Området för Södra Finlands RFV"
}
         */
        ResponseHelper.writeResponse(params, response);
    }

}

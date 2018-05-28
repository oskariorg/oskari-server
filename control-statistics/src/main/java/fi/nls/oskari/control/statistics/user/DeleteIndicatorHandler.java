package fi.nls.oskari.control.statistics.user;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.statistics.StatisticsHelper;
import fi.nls.oskari.control.statistics.GetIndicatorMetadataHandler;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.statistics.user.StatisticalIndicatorService;

/**
 * Deletes indicator that the user has previously saved.
 * Only allows deletion of the users own indicators
 */
@OskariActionRoute("DeleteIndicator")
public class DeleteIndicatorHandler extends ActionHandler {
    private static final Logger LOG = LogFactory.getLogger(DeleteIndicatorHandler.class);
    private StatisticalIndicatorService indicatorService;

    @Override
    public void init() {
        super.init();
        if (indicatorService == null) {
            indicatorService = OskariComponentManager.getComponentOfType(StatisticalIndicatorService.class);
        }
    }

    public void handleAction(ActionParameters params) throws ActionException {
        // user indicators are user content so deleting one requires to be logged in
        params.requireLoggedInUser();

        int datasourceId = params.getRequiredParamInt(StatisticsHelper.PARAM_DATASOURCE_ID);
        int id = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        StatisticalIndicator ind = indicatorService.findById(id, params.getUser().getId());
        if(ind == null) {
            // or might not be the owner
            throw new ActionDeniedException("Unknown indicator/not the owner: " + id );
        }
        JSONObject selectors = params.getHttpParamAsJSON(StatisticsHelper.PARAM_SELECTORS);
        if(selectors != null) {
            // if selectors present, regionset is also required
            int regionset = params.getRequiredParamInt("regionset");
            try {
                indicatorService.deleteIndicatorData(id, regionset, selectors.getInt("year"));
            } catch (JSONException ex) {
                throw new ActionParamsException("Year was not part of the selectors parameter");
            }

            StatisticsHelper.flushDataFromCache(datasourceId, Integer.toString(id), regionset, selectors);
        } else if (!indicatorService.delete(id, params.getUser().getId())) {
            // remove the whole indicator
            throw new ActionParamsException("Indicator wasn't removed: " +  + id);
        }
        LOG.info("Deleted indicator", id);
        try {
            ResponseHelper.writeResponse(params, GetIndicatorMetadataHandler.toJSON(ind));
        } catch (JSONException ex) {
            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("deleted", id));
        }

    }
}

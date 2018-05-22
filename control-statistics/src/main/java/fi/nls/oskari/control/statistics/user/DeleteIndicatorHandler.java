package fi.nls.oskari.control.statistics.user;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.statistics.GetIndicatorMetadataHandler;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
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

        int id = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        StatisticalIndicator ind = indicatorService.findById(id, params.getUser().getId());
        if(ind == null) {
            // or might not be the owner
            throw new ActionDeniedException("Unknown indicator/not the owner: " + id );
        }
        int year = params.getHttpParam("year", -1);
        if(year != -1) {
            // if year present, regionset is also required
            int regionset = params.getRequiredParamInt("regionset");
            indicatorService.deleteIndicatorData(id, regionset, year);
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
